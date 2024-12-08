(ns cljrpgengine.menu.equip
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.finish-equip :as finish-equip-menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(defn create
  [mob-key equipment-position]
  (let [window (ui/create-window 0 0 constants/screen-width constants/screen-height)
        equipment-choices (filter (fn [item]
                                    (or (= (:position item) equipment-position)
                                        (and (some #{:right-hand :left-hand} [equipment-position])
                                             (some #{:weapon :shield} [(:position item)]))))
                                  (map (fn [item-key]
                                         (get @item/items item-key))
                                       (keys @player/items)))
        i (atom 2)]
    (menu/create-menu
     :equip
     window
     (into [(menu/create-option
             (ui/create-label "Go back" constants/left-cursor-padding (ui/line-number window 1))
             #(menu/remove-menu!))]
           (mapv (fn [equipment]
                   (menu/create-option
                    (ui/create-label (:name equipment) constants/left-cursor-padding (ui/line-number window (swap! i inc)))
                    #(menu/add-menu! (finish-equip-menu/create mob-key equipment-position equipment))))
                 equipment-choices)))))
