(ns cljrpgengine.menus.fight.player-select-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]
            [cljrpgengine.window :as window]
            [cljrpgengine.menus.fight.action-select-menu :as action-select-menu])
  (:import (java.awt Color)
           (java.awt.geom RoundRectangle2D$Double)))

(defn- draw-atb-gauge
  [row width color to-fill]
  (.setColor @window/graphics color)
  (if to-fill
    (.fill @window/graphics
           (RoundRectangle2D$Double.
            (* constants/quarter-width 3)
            (+ (* constants/quarter-height 3) (* constants/line-spacing row) constants/text-size)
            width
            10
            3
            3))
    (.draw @window/graphics
           (RoundRectangle2D$Double.
            (* constants/quarter-width 3)
            (+ (* constants/quarter-height 3) (* constants/line-spacing row) constants/text-size)
            width
            10
            3
            3))))

(deftype PlayerSelectMenu []
  menu/Menu
  (draw [menu]
    (ui/draw-window
     constants/quarter-width (* constants/quarter-height 3)
     (* 3 constants/quarter-width) constants/quarter-height)
    (let [party (vec (vals @player/party))]
      (doseq [p (range 0 (count party))]
        (ui/draw-line constants/quarter-width
                      (* constants/quarter-height 3)
                      p
                      (str (ui/text-fixed-width (get-in party [p :name]) 15)
                           (ui/text-fixed-width (str (get-in party [p :hp]) "/" (get-in party [p :max-hp])) 10)
                           (get-in party [p :mana]) "/" (get-in party [p :max-mana]))
                      (if (util/is-party-member-atb-full? p)
                        :font-default
                        :font-disabled))
        (draw-atb-gauge p constants/atb-width Color/DARK_GRAY true)
        (draw-atb-gauge p (get @util/player-atb-gauge p) Color/LIGHT_GRAY true)
        (draw-atb-gauge p constants/atb-width Color/GRAY false)))
    (let [cursor (ui/get-menu-cursor (.menu-type menu))]
      (if (util/is-party-member-atb-full? cursor)
        (ui/draw-cursor constants/quarter-width
                        (* constants/quarter-height 3)
                        cursor)
        (ui/inc-cursor!))))
  (cursor-length [_] (count @player/party))
  (menu-type [_] :fight-party-select)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor (.menu-type menu))]
      (if (util/is-party-member-atb-full? cursor)
        (ui/open-menu! (action-select-menu/create-menu cursor))))))

(defn create-menu
  []
  (PlayerSelectMenu.))
