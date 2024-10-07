(ns cljrpgengine.menus.main-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(deftype MainMenu [state]
  menu/Menu
  (draw [menu]
    (let [padding-x (/ constants/screen-width 10)
          padding-y (/ constants/screen-height 10)
          width (- constants/screen-width (* padding-x 2))
          height (- constants/screen-height (* padding-y 2))
          menu-item (partial ui/draw-line padding-x padding-y)
          cursor (ui/get-menu-cursor state (.menu-type menu))]
      (ui/draw-window padding-x padding-y width height)
      (ui/draw-cursor padding-x padding-y cursor)
      (menu-item 0 "Continue")
      (menu-item 1 "New Game")
      (menu-item 2 "Load Game")
      (menu-item 3 "Settings")
      (menu-item 4 "Quit")))
  (cursor-length [_] 5)
  (menu-type [_] :main-menu)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))])))

(defn create-menu
  [state]
  (MainMenu. state))
