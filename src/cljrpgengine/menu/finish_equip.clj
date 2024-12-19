(ns cljrpgengine.menu.finish-equip
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def window-padding 60)

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
    (menu/create-menu-2
     :finish-equip
     window
     [(menu/create-option
       (ui/create-label "Ok" constants/left-cursor-padding (ui/line-number window 3))
       (fn []
         (menu/remove-menu! 2)
         ((:on-change (last @menu/opened-menus))
          (menu/create-event :updated
                             {:equipment-position equipment-position
                              :equipment equipment}))))])))
