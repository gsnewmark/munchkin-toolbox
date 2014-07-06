(ns munchkin-toolbox.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def app-state (atom {:text "Hello world!"}))

(when-let [dom-el (.getElementById js/document "app")]
  (om/root (fn [app owner]
             (dom/h1 nil (:text app)))
           app-state
           {:target dom-el}))
