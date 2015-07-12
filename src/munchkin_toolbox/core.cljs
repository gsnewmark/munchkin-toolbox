(ns munchkin-toolbox.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [om.core :as om]
            [cljs.core.async :refer [put! chan <!]]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

(defn default-player
  [n]
  "Creates a default description for n-th player."
  {:name (str "Player " n)
   :level 1
   :strength 0
   :wins 0})

(def ^:private local-storage-key "players")

(defn- load-players []
  (-> js/localStorage
      (.getItem local-storage-key)
      (#(.parse js/JSON %))
      (js->clj :keywordize-keys true)
      (#(when %
          (mapv merge (repeatedly (count %) (partial default-player "0")) %)))))

(def ^:private winning-level 10)

(defonce app-state
  (atom
   {:players (or (load-players) (mapv default-player (range 1 4)))}))


(defn level-counter
  "Editable level counter."
  [level owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (let [level-changed-c (:level-changed state)]
        (html
         [:div.uk-grid
          [:div.uk-width-2-10
           [:a
            {:class (str "uk-icon-arrow-down uk-text-danger "
                         "uk-icon-hover uk-icon-small uk-icon-button")
             :type "button"
             :on-click #(put! level-changed-c ::dec)}]]
          [:div.uk-width-6-10
           [:div.level-progress.uk-progress.uk-progress-warning
            [:div {:class "uk-progress-bar"
                   :style {:width (str (* level (/ 100 winning-level)) "%")}}
             [:span.level.uk-text-large level]]]]
          [:div.uk-width-2-10
           [:a
            {:class (str "uk-icon-arrow-up uk-text-success "
                         "uk-icon-hover uk-icon-small uk-icon-button")
             :type "button"
             :on-click #(put! level-changed-c ::inc)}]]])))))

(defn strength-indicator
  "Editable strength indicator."
  [player owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (let [strength-changed-c (:strength-changed state)
            strength (max (+ (:level player) (:strength player)) 1)]
        (html
         [:div.uk-grid
          [:div.uk-width-1-3
           [:a
            {:class (str "uk-icon-minus uk-text-danger "
                         "uk-icon-hover uk-icon-small uk-icon-button")
             :type "button"
             :on-click #(put! strength-changed-c ::dec)}]]
          [:div.uk-width-1-3
           [:span.strength strength]]
          [:div.uk-width-1-3
           [:a
            {:class (str "uk-icon-plus uk-text-success "
                         "uk-icon-hover uk-icon-small uk-icon-button")
             :type "button"
             :on-click #(put! strength-changed-c ::inc)}]]])))))

(defn editable-label
  [label-key]
  (fn [data owner]
    (reify
      om/IInitState
      (init-state [_]
        {:edit? false
         :label-changed (chan)})
      om/IWillMount
      (will-mount [_]
        (let [label-changed-c (om/get-state owner :label-changed)]
          (go (loop []
                (let [name (<! label-changed-c)]
                  (om/update! data label-key name)
                  (om/set-state! owner :edit? false)
                  (recur))))))
      om/IRenderState
      (render-state [_ state]
        (let [{:keys [edit? label label-changed]} state
              label (or label (get data label-key ""))]
          (html
           (if edit?
             [:form.uk-form
              [:fieldset {:data-uk-margin ""}
               [:input
                {:type "text"
                 :placeholder "Player's name"
                 :on-change
                 #(om/set-state! owner :label (.. % -target -value))
                 :value label}]
               " "
               [:button
                {:class "uk-button uk-button-primary"
                 :type "button"
                 :on-click #(put! label-changed label)}
                "Save"]]]
             [:label.editable
              {:on-click #(om/set-state! owner :edit? true)}
              (get data label-key "")])))))))

(defmulti change-level identity)
(defmethod change-level ::inc [op] (fn [l] (if (< l winning-level) (inc l) l)))
(defmethod change-level ::dec [op] (fn [l] (if (> l 1) (dec l) l)))

(defmulti change-strength identity)
(defmethod change-strength ::inc [op] inc)
(defmethod change-strength ::dec [op] (fn [s] (if (> s 0) (dec s) s)))

(defn player-info
  "Card with info about the player."
  [{:keys [name level wins] :as player} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:level-changed (chan)
       :strength-changed (chan)})
    om/IWillMount
    (will-mount [_]
      (let [level-changed-c (om/get-state owner :level-changed)
            strength-changed-c (om/get-state owner :strength-changed)]
        (go (loop []
              (go (loop []
                    (alt!
                      level-changed-c
                      ([op]
                       (om/transact!
                        player
                        (fn [p]
                          (let [level ((change-level op) (:level p))]
                            (-> p
                                (assoc :level level)
                                (update-in [:wins]
                                           (if (>= level winning-level)
                                             inc
                                             identity)))))))
                      strength-changed-c
                      ([op] (om/transact! player :strength (change-strength op))))
                    (recur)))))))
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.uk-width-medium-1-3.uk-width-small-1-2.uk-grid-margin
        [:div.card.uk-panel.uk-panel-box
         [:h3.uk-panel-title
          (om/build (editable-label :name) player)
          (when (> wins 0)
            [:div "Wins: " wins])]
         (when (>= level winning-level)
           [:div.uk-panel-badge [:i.uk-text-warning.uk-icon-trophy.uk-icon-medium]])
         (om/build level-counter
                   level
                   {:init-state (select-keys state [:level-changed])})
         [:hr]
         [:div.uk-text-center
          (om/build strength-indicator
                    player
                    {:init-state (select-keys state [:strength-changed])})]]]))))

(defn players-list
  "List of players' info."
  [data owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div.uk-grid
        (om/build-all player-info (get data :players []) {:key :name})]))))


(defn- reset-players
  [players]
  (let [new-player-data (dissoc (default-player 0) :name :wins)]
    (mapv #(merge % new-player-data) players)))

(defn- save-players [players]
  (let [players (->> players
                     reset-players
                     clj->js
                     (.stringify js/JSON))]
    (.setItem js/localStorage local-storage-key players)))

(defn control-panel
  [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:player-added (chan)
       :player-reset (chan)
       :wins-reset (chan)
       :player-save (chan)
       :player-load (chan)})
    om/IWillMount
    (will-mount [_]
      (let [player-added-c (om/get-state owner :player-added)
            player-reset-c (om/get-state owner :player-reset)
            wins-reset-c (om/get-state owner :wins-reset)
            player-save-c (om/get-state owner :player-save)
            player-load-c (om/get-state owner :player-load)]
        (go (loop []
              (alt!
                player-added-c
                ([op] (om/transact! data :players
                                    (fn [p]
                                      (let [n (inc (count p))]
                                        (conj p (default-player n))))))
                player-reset-c
                ([op]
                 (om/transact! data :players reset-players))

                wins-reset-c
                ([op]
                 (om/transact! data :players
                               (fn [p] (mapv #(assoc % :wins 0) p))))

                player-save-c
                ([_] (save-players (:players @data)))

                player-load-c
                ([_]
                 (when-let [players (load-players)]
                   (om/update! data :players players))))
              (recur)))))
    om/IRenderState
    (render-state [_ state]
      (let [player-added-c (:player-added state)
            player-reset-c (:player-reset state)
            wins-reset-c (:wins-reset state)
            player-save-c (:player-save state)
            player-load-c (:player-load state)]
        (html
         [:nav.uk-navbar
          [:button
           {:class "uk-button uk-button-success"
            :type "button"
            :on-click #(put! player-added-c true)}
           "Add a new player"]
          " "
          [:button
           {:class "uk-button uk-button-danger"
            :type "button"
            :on-click #(put! player-reset-c true)}
           "Reset player stats"]
          " "
          [:button
           {:class "uk-button uk-button-danger"
            :type "button"
            :on-click #(put! wins-reset-c true)}
           "Reset wins"]
          [:div.uk-navbar-flip
           [:button
            {:class "uk-button uk-button-primary"
             :type "button"
             :on-click #(put! player-save-c true)}
            "Save players"]
           " "
           [:button
            {:class "uk-button uk-button-danger"
             :type "button"
             :on-click #(put! player-load-c true)}
            "Load players"]]])))))


(defn app
  [data owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div
        (om/build control-panel data)
        (om/build players-list data)]))))


(defn start []
  (when-let [dom-el (.getElementById js/document "app")]
    (om/root app
             app-state
             {:target dom-el})))
