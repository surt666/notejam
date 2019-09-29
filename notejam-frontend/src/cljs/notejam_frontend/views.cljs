(ns notejam-frontend.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [notejam-frontend.subs :as subs]
   [reagent.core :as reagent]))

(defn title []
  [re-com/title
   :label "Notejam:"
   :level :level1])

(def username (reagent/atom ""))
(def password (reagent/atom ""))

(defn username-comp []
  (fn []
    [re-com/h-box
     :gap "10px"
     :children [[re-com/box
                 :child
                 [re-com/label
                  :label "Username:"]]
                [re-com/box
                 :child
                 [re-com/input-text
                  :model username
                  :on-change #(reset! username %)]]]]))

(defn password-comp []
  (fn []
   [re-com/h-box
    :gap "10px"
    :children [[re-com/box
                :child
                [re-com/label
                 :label "Password:"]]
               [re-com/box
                :child
                [re-com/input-password
                 :model password
                 :on-change #(reset! password %)]]]]))

(defn login-comp []
  (fn []
    [re-com/v-box
     :height "100%"
     :gap "10px"
     :children
     [[username-comp]
      [password-comp]
      [re-com/button
       :label "Login"
       :on-click #(re-frame/dispatch [:login @username @password])]]]))

(defn pad-comp []
  (let [pads (re-frame/subscribe [::subs/pads])]
    (prn "PADS" @pads)
    (fn []
     [re-com/v-box
      :children
      [[re-com/title
        :label "Pads"
        :level :level3]
       (for [pad @pads]
         [re-com/hyperlink :label (pad :pad-name) :on-click #(re-frame/dispatch [:show-notes (pad :pk)])])]])))

(defn note-comp []
  (let [notes (re-frame/subscribe [::subs/notes])]
    (fn []
     [re-com/v-box
      :children
      [(for [note @notes]
         [re-com/hyperlink :label (note :note-name) :on-click #(re-frame/dispatch [:show-note (note :pk)])])]])))

(defn note-view []
  (fn []
    [re-com/h-box
     :children
     [[pad-comp]
      [note-comp]]]))

(defn choose-view []
  (let [logged-in? (re-frame/subscribe [::subs/logged-in?])]
    (prn "LOG" @logged-in?)
    (if (not @logged-in?)
      [[title]
       [login-comp]]
      [[title]
       [note-view]])))

(defn main-panel []
  [re-com/v-box
   :height "100%"
   :gap "20px"
   :children (choose-view)])
