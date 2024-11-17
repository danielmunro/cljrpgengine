(ns cljrpgengine.menus.fight.gains-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.fight :as fight]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(deftype GainsMenu []
  menu/Menu
  (draw [_]
    (ui/draw-window
     0 0
     constants/screen-width (/ constants/screen-height 10))
    (ui/draw-line 0 0
                  0
                  (format "Party gained %d xp" @fight/xp-to-gain)))
  (cursor-length [_] 0)
  (menu-type [_] :gains)
  (key-pressed [_]
    (dosync
     (doseq [i (keys @player/party)]
       (swap! player/party update-in [i :xp] (fn [xp] (+ xp @fight/xp-to-gain)))))
    (ui/close-menu! 2)
    (fight/end)))

(defn create-menu
  []
  (GainsMenu.))
