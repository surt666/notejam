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
    (fn []
      [re-com/v-box
       :gap "20px"
       :children
       [[re-com/title
         :label "My pads"
         :level :level3]
        (for [pad @pads]
          [re-com/hyperlink :label (pad :pad-name) :on-click #(re-frame/dispatch [:show-notes (pad :pk)])])]])))

(defn note-comp []
  (let [notes (re-frame/subscribe [::subs/notes])
        note-name (re-frame/subscribe [::subs/note-name])
        note-text (re-frame/subscribe [::subs/note-text])
        edit-mode? (re-frame/subscribe [::subs/edit-mode?])
        pad-id (re-frame/subscribe [::subs/pad-id])
        nt (reagent/atom "")
        nn (reagent/atom "")]
    (fn []
      [re-com/v-box
       :gap "10px"
       :children
       (cond
         (or (not= "" @note-text)) [[re-com/title
                                :label @note-name
                                :level :level4]                               
                               [re-com/input-textarea
                                :model ((fn [] (reset! nt @note-text) nt))
                                :disabled? (not @edit-mode?) 
                                :on-change #(reset! nt %)]
                               (if @edit-mode?
                                 [re-com/button
                                  :label "Create"
                                  :on-click #(re-frame/dispatch [:create @note-name @nt])]
                                 [re-com/button
                                  :label "Edit"
                                  :on-click #(re-frame/dispatch [:edit])])]
         (and @edit-mode?
              (= "" @note-text)) [[re-com/input-text
                                   :model nn
                                   :on-change #(reset! nn %)]
                                  [re-com/input-textarea
                                   :model nt
                                   :on-change #(reset! nt %)]
                                  [re-com/button
                                   :label "Create"
                                   :on-click #(re-frame/dispatch [:create @nn @nt])]]
         :default [[re-com/title
                    :label "Notes"
                    :level :level4]
                   (for [note @notes]
                     [re-com/hyperlink :label (note :note-name) :on-click #(re-frame/dispatch [:show-note (note :pk)])])
                   (if (not= "" @pad-id)
                     [re-com/button
                     :label "New note"
                     :on-click #(re-frame/dispatch [:new])])])])))

(defn note-view []
  (fn []
    [re-com/h-box
     :gap "20px"
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
   :margin "40px"
   :gap "20px"
   :children (choose-view)])
