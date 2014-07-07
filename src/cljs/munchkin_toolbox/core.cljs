(ns munchkin-toolbox.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :refer [put! chan <! go]]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

(defn default-player
  [n]
  "Creates a default description for n-th player."
  {:name (str "Player " n)
   :level 1})

(def app-state
  (atom {:players (mapv default-player (range 1 4))}))


(defn level-counter
  "Editable level counter."
  [level owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (let [level-changed-c (:level-changed state)]
        (html
         [:div {:class "row"}
          [:div {:class "col-sm-2"}
           [:button
            {:class "btn btn-success btn-lg btn-block"
             :on-click #(put! level-changed-c ::dec)}
            "-"]]
          [:div {:class "col-sm-8"}
           [:div {:class "progress"}
            [:div {:class "progress-bar"
                   :role "progressbar"
                   :aria-valuenow level
                   :aria-valuemin "0"
                   :aria-valuemax "10"
                   :style {:width (str (* level 10) "%")}}
             level]]]
          [:div {:class "col-sm-2"}
           [:button
            {:class "btn btn-danger btn-lg btn-block"
             :on-click #(put! level-changed-c ::inc)}
            "+"]]])))))

(defmulti change-level identity)
(defmethod change-level ::inc [op] (fn [l] (if (< l 10) (inc l) l)))
(defmethod change-level ::dec [op] (fn [l] (if (> l 1) (dec l) l)))

(defn player-info
  "Row with info about the player."
  [{:keys [name level] :as player} owner]
  (reify
    om/IInitState
    (init-state [_] {:level-changed (chan)})
    om/IWillMount
    (will-mount [_]
      (let [level-changed-c (om/get-state owner :level-changed)]
        (go (loop []
              (let [op (<! level-changed-c)]
                (om/transact! player :level (change-level op))
                (recur))))))
    om/IRenderState
    (render-state [_ state]
      (html
       [:div {:class "row"}
        [:div {:class "row"} name]
        (om/build level-counter
                  level
                  {:init-state (select-keys state [:level-changed])})
        [:hr]]))))

(defn players-list
  "List of players' info."
  [data owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div {:class "row"}
        (om/build-all player-info (get data :players []))]))))


(defn control-panel
  [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:player-added (chan)})
    om/IWillMount
    (will-mount [_]
      (let [player-added-c (om/get-state owner :player-added)]
        (go (loop []
              (let [op (<! player-added-c)]
                (om/transact! data :players
                              (fn [p]
                                (let [n (inc (count p))]
                                  (conj p (default-player n)))))
                (recur))))))
    om/IRenderState
    (render-state [_ state]
      (let [player-added-c (:player-added state)]
        (html
         [:div {:class "row"}
          [:button
           {:class "btn btn-default"
            :on-click #(put! player-added-c true)}
           "Add a new player"]])))))


(defn app
  [data owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div {:class "container"}
        (om/build control-panel data)
        (om/build players-list data)]))))


(when-let [dom-el (.getElementById js/document "app")]
  (om/root app
           app-state
           {:target dom-el}))
