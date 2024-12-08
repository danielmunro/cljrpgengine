(ns cljrpgengine.menu.finish-equip
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def window-padding 60)
(def x-padding 30)

(defn create
  [mob-key equipment-position equipment]
  (let [window (ui/create-window window-padding
                                 window-padding
                                 (- constants/screen-width (* 2 window-padding))
                                 (- constants/screen-height (* 2 window-padding)))
        mob (get @player/party mob-key)]
    (if-let [equipped (get @(:equipment mob) equipment-position)]
      (player/add-item! equipped))
    (swap! (:equipment mob) assoc equipment-position (:identifier equipment))
    (player/remove-item! (:identifier equipment))
    (.addActor window (ui/create-label (str "You equip " (:name equipment))
                                       constants/padding
                                       (ui/line-number window 1)))
    (menu/create-menu
     :finish-equip
     window
     [(menu/create-option
       (ui/create-label "Ok" x-padding (ui/line-number window 3))
       #(menu/remove-menu!))])))
