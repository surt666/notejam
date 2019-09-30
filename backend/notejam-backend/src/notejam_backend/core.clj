(ns notejam-backend.core
  (:gen-class
   :methods [^:static [handler [java.util.Map] java.util.Map]])
  (:require [cheshire.core :refer :all]           
            [amazonica.aws.dynamodbv2 :as ddb]
            [notejam-common-layer.core :refer :all]))

(defn get-tx [type]
  (get-in (ddb/update-item
           :table-name "counter"
           :key {:type type}
           :update-expression "SET seq = seq + :incr"
           :expression-attribute-values {":incr" 1}
           :return-values "UPDATED_NEW") [:attributes :seq]))

(defn search-table [search-term]
  (:items
   (ddb/query :table-name "notejam"
              :select "ALL_ATTRIBUTES"
              :scan-index-forward true
              :key-conditions
              {:pk {:attribute-value-list [search-term] :comparison-operator "EQ"}})))

(defn search-index [search-term]
  (:items
   (ddb/query :table-name "notejam"
              :select "ALL_ATTRIBUTES"
              :index-name "sk-index"
              :scan-index-forward true
              :key-conditions
              {:sk {:attribute-value-list [search-term] :comparison-operator "EQ"}})))

(defn insert [event]
  (let [type (event :type)
        event-pk (event :pk)
        id (when (and (or (= "" event-pk) (nil? event-pk)) (not= type "user")) (get-tx type))
        data (dissoc event :type :rel :email :action :pk)
        pk (cond
             (= type "user") (event :email)
             (and (not= "" event-pk) (not (nil? event-pk))) event-pk
             :default (str type "-" id))
        item-list [{:put-request
                    {:item (merge {:pk pk
                                   :sk pk} data)}}
                   (if (= type "pad")
                     {:put-request
                      {:item {:pk pk
                              :sk (when (event :rel) (event :rel))
                              :pad-name (event :pad-name)}}}
                     {:put-request
                      {:item {:pk pk
                              :sk (when (event :rel) (event :rel))
                              :note-name (event :note-name)}}})]]
    (prn id (not= "" event-pk) (not (nil? event-pk)) event-pk type pk item-list)
    (ddb/batch-write-item
     :return-consumed-capacity "TOTAL"
     :return-item-collection-metrics "SIZE"
     :request-items {"notejam" (filter #(not (nil? %)) item-list)})))

(defn check-user [event]
  (let [resp (:item
              (ddb/get-item :table-name "notejam"
                            :key {:pk {:s (event :user-id)}
                                  :sk {:s (event :user-id)}}))]
    {"logged-in" (= (resp :password) (event :password))}))

(defn stringify-keys
  "Recursively transforms all map keys from keywords to strings."
  [m]
  (let [f (fn [[k v]] (if (keyword? k) [(name k) v] [k v]))]
    (clojure.walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn find-pads [event]
  (stringify-keys {:pads (vec (filter #(= "pad" (first (clojure.string/split (% :pk) #"-"))) (search-index (event :user-id))))}))

(defn find-notes [event]
  (stringify-keys {:notes (vec (filter #(= "note" (first (clojure.string/split (% :pk) #"-"))) (search-index (event :pad-id))))}))

(defn find-note [event]
  (stringify-keys
   (:item
    (ddb/get-item :table-name "notejam"
                  :key {:pk {:s (event :note-id)}
                        :sk {:s (event :note-id)}}))))

(defn -handler [e]
  (prn e)
  (let [event (as-clj-map e)
        action (event :action)]
    (cond
      (= action "insert") (insert event)
      (= action "check-user") (check-user event)
      (= action "find-pads") (find-pads event)
      (= action "find-notes") (find-notes event)
      (= action "find-note") (find-note event))))

