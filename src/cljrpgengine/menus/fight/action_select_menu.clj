(ns cljrpgengine.menus.fight.action-select-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.fight.target-beast-menu :as target-beast-menu]
            [cljrpgengine.menus.fight.magic-select-menu :as magic-select-menu]))

(def menu-width constants/quarter-width)
(def menu-height 120)
(def menu-x 0)
(def menu-y (- constants/screen-height menu-height))

(deftype ActionSelectMenu [party-index]
  menu/Menu
  (draw [menu]
    (ui/draw-window
     menu-x menu-y
     menu-width menu-height)
    (ui/draw-line menu-x menu-y
                  0
                  "Attack")
    (ui/draw-line menu-x menu-y
                  1
                  "Magic")
    (ui/draw-line menu-x menu-y
                  2
                  "Defend")
    (ui/draw-line menu-x menu-y
                  3
                  "Item")
    (ui/draw-line menu-x menu-y
                  4
                  "Flee")
    (let [cursor (ui/get-menu-cursor (.menu-type menu))]
      (ui/draw-cursor menu-x
                      menu-y
                      cursor)))
  (cursor-length [_] 5)
  (menu-type [_] :fight-action-select)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor (.menu-type menu))]
      (cond
        (= 0 cursor)
        (ui/open-menu! (target-beast-menu/create-menu party-index))
        (= 1 cursor)
        (ui/open-menu! (magic-select-menu/create-menu party-index))))))

(defn create-menu
  [party-index]
  (ActionSelectMenu. party-index))

