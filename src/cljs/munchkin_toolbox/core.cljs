(ns munchkin-toolbox.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

(def app-state (atom {:text "Hello world!"}))

(when-let [dom-el (.getElementById js/document "app")]
  (om/root (fn [app owner] (html [:h1 (:text app)]))
           app-state
           {:target dom-el}))
