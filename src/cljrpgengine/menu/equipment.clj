(ns cljrpgengine.menu.equipment
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.equip :as equip-menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]))

(defn create
  [mob-key]
  (let [{:keys [portrait]} (get @player/party mob-key)
        mob (get @player/party mob-key)
        window (ui/create-window
                0
                0
                constants/screen-width
                constants/screen-height)
        i (atom 2)
        x-padding (+ (.getWidth portrait)
                     constants/padding
                     constants/left-cursor-padding)]
    (.addActor window (util/create-image portrait constants/padding (- constants/screen-height
                                                                       (.getHeight portrait)
                                                                       constants/padding)))
    (.addActor window (ui/create-label (str "Equipment for " (:name mob) ": ")
                                       x-padding
                                       (ui/line-number window 1)))
    (menu/create-menu
     :equipment
     window
     (mapv (fn [equipment-position]
             (menu/create-option
              (ui/create-label (str (ui/text-fixed-width (name equipment-position) 15)
                                    (if-let [equipment (get @(:equipment mob) equipment-position)]
                                      (:name (get @item/items equipment))
                                      "none"))
                               x-padding
                               (ui/line-number window (swap! i inc)))
              #(menu/add-menu! (equip-menu/create mob-key equipment-position))))
           (keys @(:equipment mob))))))
