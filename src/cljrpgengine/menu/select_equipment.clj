(ns cljrpgengine.menu.select_equipment
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.common :as common]
            [cljrpgengine.menu.finish-equip :as finish-equip-menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui])
  (:import (com.badlogic.gdx.scenes.scene2d Group)))

(defn create
  [mob-key equipment-position]
  (let [window (ui/create-window 0 0 (* constants/screen-width 2/3) constants/screen-height)
        equipment-choices (filterv (fn [item]
                                     (or (= (:position item) equipment-position)
                                         (and (some #{:right-hand :left-hand} [equipment-position])
                                              (some #{:weapon :shield} [(:position item)]))))
                                   (map (fn [item-key]
                                          (get @item/items item-key))
                                        (keys @player/items)))
        i (atom 0)
        mob (get @player/party mob-key)
        equipped (get @(:equipment mob) equipment-position)
        initial-options (if equipped
                          [(menu/create-option
                            (ui/create-label (str "remove " (:name (get @item/items equipped)))
                                             constants/left-cursor-padding
                                             (ui/line-number window (swap! i inc)))
                            (fn []
                              (let [equipment (:equipment mob)]
                                (swap! equipment
                                       (fn [equipment]
                                         (player/add-item! (get equipment equipment-position))
                                         (assoc equipment equipment-position nil)))
                                (menu/remove-menu!)
                                ((:on-change (last @menu/opened-menus))
                                 (menu/create-event :updated
                                                    {:equipment-position equipment-position
                                                     :equipment equipment})))))]
                          [])
        options (into initial-options
                      (mapv (fn [equipment]
                              (menu/create-option
                               (ui/create-label (:name equipment)
                                                constants/left-cursor-padding
                                                (ui/line-number window (swap! i inc)))
                               #(menu/add-menu! (finish-equip-menu/create mob-key equipment-position equipment))))
                            equipment-choices))
        attributes-pane (doto (Group.)
                          (.setX (* constants/screen-width 2/3)))
        compare (fn [choice-index]
                  (let [equipped-item (get @item/items equipped)
                        compare-item (get equipment-choices choice-index)]
                    (item/compare-equipment equipped-item compare-item)))]
    (.addActor window attributes-pane)
    (common/draw-attributes window attributes-pane mob (compare (if equipped nil 0)))
    (menu/create-menu
     :equip
     window
     (if (not-empty options)
       options
       [(menu/create-option
         (ui/create-label "(no equipment)"
                          constants/left-cursor-padding
                          (ui/line-number window 1))
         #(menu/remove-menu!))])
     (fn [{:keys [event-type changed]}]
       (when (= event-type :cursor)
         (common/draw-attributes window attributes-pane mob (compare (if equipped (dec changed) changed))))))))
