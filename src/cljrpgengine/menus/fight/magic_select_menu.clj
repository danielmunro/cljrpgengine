(ns cljrpgengine.menus.fight.magic-select-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(deftype MagicSelectMenu [state player-index]
  menu/Menu
  (draw [menu]
    (ui/draw-window
     constants/quarter-width (* constants/quarter-height 3)
     (* 3 constants/quarter-width) constants/quarter-height)
    (ui/draw-cursor
     constants/quarter-width
     (* constants/quarter-height 3)
     (ui/get-menu-cursor (.menu-type menu))))
  (cursor-length [_] (count (get-in @player/party [(nth (vals @player/party) player-index) :spells])))
  (menu-type [_] :fight-magic-select)
  (key-pressed [_]))

(defn create-menu
  [state player-index]
  (MagicSelectMenu. state player-index))
