(ns cljrpgengine.menu.select_equipment
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.finish-equip :as finish-equip-menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def x (/ constants/screen-width 2))

(defn create
  [mob-key equipment-position]
  (let [window (ui/create-window x 0 (- constants/screen-width x) constants/screen-height)
        equipment-choices (filter (fn [item]
                                    (or (= (:position item) equipment-position)
                                        (and (some #{:right-hand :left-hand} [equipment-position])
                                             (some #{:weapon :shield} [(:position item)]))))
                                  (map (fn [item-key]
                                         (get @item/items item-key))
                                       (keys @player/items)))
        i (atom 0)
        equipped (get @(:equipment (get @player/party mob-key)) equipment-position)
        options (if equipped
                  [(menu/create-option
                    (ui/create-label (str "remove " (:name (get @item/items equipped))) constants/left-cursor-padding (ui/line-number window (swap! i inc)))
                    (fn []
                      (let [equipment (:equipment (get @player/party mob-key))]
                        (swap! equipment
                               (fn [equipment]
                                 (player/add-item! (get equipment equipment-position))
                                 (assoc equipment equipment-position nil)))
                        (menu/remove-menu!)
                        ((:on-change (last @menu/opened-menus))
                         (menu/create-event :updated
                                            {:equipment-position equipment-position
                                             :equipment equipment})))))]
                  [])]
    (menu/create-menu
     :equip
     window
     (into options
           (mapv (fn [equipment]
                   (menu/create-option
                    (ui/create-label (:name equipment) constants/left-cursor-padding (ui/line-number window (swap! i inc)))
                    #(menu/add-menu! (finish-equip-menu/create mob-key equipment-position equipment))))
                 equipment-choices)))))
