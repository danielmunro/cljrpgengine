(ns cljrpgengine.menu.use-item
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.select-party :as select-party-menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def x-padding 200)
(def y-padding 80)

(defn- get-restore-amount
  [mob max-amount attr]
  (min max-amount (- ((:calc-attr mob) attr)
                     @(get mob attr))))

(defn- apply-affect
  [mob {:keys [affect amount]}]
  (case affect
    :restore-hp
    (let [gain (get-restore-amount mob amount :hp)]
      (swap! (:hp mob) (fn [hp] (+ hp gain)))
      (str "You restored " gain " hp!"))
    :restore-mana
    (let [gain (get-restore-amount mob amount :mana)]
      (swap! (:mana mob) (fn [mana] (+ mana gain)))
      (str "You restored " gain " mana!"))
    (str "Unknown item affect in create-apply-item: " affect)))

(defn- create-apply-item
  [item mob-key]
  (let [mob (get @player/party mob-key)
        window (ui/create-window 100 100 (- constants/screen-width 200) (- constants/screen-height 200))
        options [(menu/create-option
                  (ui/create-label "Ok"
                                   constants/left-cursor-padding
                                   (ui/line-number window 3))
                  #(menu/remove-menu! 2))]
        cursor (ui/create-cursor)]
    (player/remove-item! (:identifier item))
    (.addActor window (ui/create-label
                       (apply-affect mob item)
                       0
                       (ui/line-number window 1)))
    (doseq [option options]
      (.addActor window (:label option)))
    (.addActor window (:image cursor))
    (menu/create-menu-2
     :apply-item
     window
     nil
     options
     cursor
     #())))

(defn- create-label-for-item
  [window index {:keys [affect amount]}]
  (let [mob (nth (vals @player/party) index)]
    (cond
      (= :restore-hp affect)
      (ui/create-label (str "+" (get-restore-amount mob amount :hp))
                       x-padding
                       (- (ui/line-number window 2)
                          (* y-padding index))
                       (:success constants/font-colors))
      (= :restore-mana affect)
      (ui/create-label (str "+" (get-restore-amount mob amount :mana))
                       x-padding
                       (- (ui/line-number window 3)
                          (* y-padding index))
                       (:success constants/font-colors)))))

(defn create
  [item-key]
  (let [item (get @item/items item-key)
        {:keys [window] :as menu} (select-party-menu/create (partial create-apply-item item))
        label (atom (create-label-for-item window
                                           @(-> menu :cursor :index)
                                           item))]
    (.addActor window @label)
    (assoc
     menu
     :on-change (fn [_]
                  (.removeActor window @label)
                  (.addActor window (swap! label (constantly
                                                  (create-label-for-item window
                                                                         @(-> menu :cursor :index)
                                                                         (get @item/items item-key)))))))))
