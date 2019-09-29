(ns notejam-frontend.events
  (:require
   [re-frame.core :as re-frame]
   [notejam-frontend.db :as db]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db))

(re-frame/reg-event-db                   
  :bad-response             
  (fn
    [db [_ response]]           ;; destructure the response from the event vector
    (-> db
        (assoc :loading? false) ;; take away that "Loading ..." UI 
        (assoc :error-data (js->clj response)))))

(re-frame/reg-event-db                   
  :login-response             
  (fn
    [db [_ response]]           ;; destructure the response from the event vector
    (prn response)
    (let [resp (js->clj response)
          logged-in? (resp :logged-in)]
      (when logged-in?
        (re-frame/dispatch [:find-pads (db :username)]))
      (-> db
         (assoc :loading? false) ;; take away that "Loading ..." UI 
         (assoc :logged-in? logged-in?)))))

(re-frame/reg-event-db                   
  :find-pads-response             
  (fn
    [db [_ response]]           ;; destructure the response from the event vector
    (-> db
        (assoc :loading? false) ;; take away that "Loading ..." UI 
        (assoc :pads (:pads response)))))

(re-frame/reg-event-db                   
  :find-notes-response             
  (fn
    [db [_ response]]           ;; destructure the response from the event vector
    (-> db
        (assoc :loading? false) ;; take away that "Loading ..." UI 
        (assoc :notes (:notes response)))))

(re-frame/reg-event-db                   
  :find-note-response             
  (fn
    [db [_ response]]
    (prn "RE" response)
    (-> db
        (assoc :loading? false) ;; take away that "Loading ..." UI 
        (assoc :note response))))

(re-frame.core/reg-event-fx   
 :login               
 (fn                
   [{db :db} [_ username password]]
   (prn "PW" username password)
   ;; we return a map of (side) effects
   {:http-xhrio {:method          :post
                 :uri             "https://c2k7l9lkog.execute-api.eu-west-1.amazonaws.com/dev"
                 :params          {:action "check-user"
                                   :user-id username
                                   :password password}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true}) 
                 :on-success      [:login-response]
                 :on-failure      [:bad-response]}
    :db  (assoc db :loading? true :username username)}))

(re-frame.core/reg-event-fx   
 :find-pads              
 (fn                
   [{db :db} [_ username]]
   ;; we return a map of (side) effects
   {:http-xhrio {:method          :post
                 :uri             "https://c2k7l9lkog.execute-api.eu-west-1.amazonaws.com/dev"
                 :params          {:action "find-pads"
                                   :user-id username}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format  {:keywords? true}
                                   ) 
                 :on-success      [:find-pads-response]
                 :on-failure      [:bad-response]}
    :db  (assoc db :loading? true)}))

(re-frame.core/reg-event-fx   
 :show-notes              
 (fn                
   [{db :db} [_ pad-id]]
   ;; we return a map of (side) effects
   {:http-xhrio {:method          :post
                 :uri             "https://c2k7l9lkog.execute-api.eu-west-1.amazonaws.com/dev"
                 :params          {:action "find-notes"
                                   :pad-id pad-id}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format  {:keywords? true}
                                   ) 
                 :on-success      [:find-notes-response]
                 :on-failure      [:bad-response]}
    :db  (assoc db :loading? true)}))

(re-frame.core/reg-event-fx   
 :show-note              
 (fn                
   [{db :db} [_ note-id]]
   ;; we return a map of (side) effects
   {:http-xhrio {:method          :post
                 :uri             "https://c2k7l9lkog.execute-api.eu-west-1.amazonaws.com/dev"
                 :params          {:action "find-note"
                                   :note-id note-id}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format  {:keywords? true}
                                   ) 
                 :on-success      [:find-note-response]
                 :on-failure      [:bad-response]}
    :db  (assoc db :loading? true)}))
