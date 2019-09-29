(ns notejam-frontend.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::username
 (fn [db]
   (:username db)))

(re-frame/reg-sub
 ::logged-in?
 (fn [db]
   (:logged-in? db)))

(re-frame/reg-sub
 ::pads
 (fn [db]
   (:pads db)))

(re-frame/reg-sub
 ::notes
 (fn [db]
   (:notes db)))
