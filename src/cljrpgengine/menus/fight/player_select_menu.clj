(ns cljrpgengine.menus.fight.player-select-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]
            [cljrpgengine.window :as window])
  (:import (java.awt Color)
           (java.awt.geom RoundRectangle2D$Double)))

(def quarter-height (/ constants/screen-height 4))
(def quarter-width (/ constants/screen-width 4))

(defn- draw-atb-gauge
  [row width color to-fill]
  (.setColor @window/graphics color)
  (if to-fill
    (.fill @window/graphics
           (RoundRectangle2D$Double.
            (* quarter-width 3)
            (+ (* quarter-height 3) (* constants/line-spacing row) constants/text-size)
            width
            10
            3
            3))
    (.draw @window/graphics
           (RoundRectangle2D$Double.
            (* quarter-width 3)
            (+ (* quarter-height 3) (* constants/line-spacing row) constants/text-size)
            width
            10
            3
            3))))

(deftype PlayerSelectMenu [state]
  menu/Menu
  (draw [menu]
    (ui/draw-window
     quarter-width (* quarter-height 3)
     (* 3 quarter-width) quarter-height)
    (let [party (get-in @state [:player :party])]
      (doseq [p (range 0 (count party))]
        (ui/draw-line quarter-width
                      (* quarter-height 3)
                      p
                      (str (ui/text-fixed-width (:name (get party p)) 15)
                           (ui/text-fixed-width (str (:hp (get party p)) "/" (:max-hp (get party p))) 10)
                           (:mana (get party p)) "/" (:max-mana (get party p)))
                      (if (util/is-party-member-atb-full? p)
                        :font-default
                        :font-disabled))
        (draw-atb-gauge p constants/atb-width Color/DARK_GRAY true)
        (draw-atb-gauge p (get @util/player-atb-gauge p) Color/LIGHT_GRAY true)
        (draw-atb-gauge p constants/atb-width Color/GRAY false)))
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))]
      (if (util/is-party-member-atb-full? cursor)
        (ui/draw-cursor quarter-width
                        (* quarter-height 3)
                        cursor)
        (ui/inc-cursor! state))))
  (cursor-length [_] (count (get-in @state [:player :party])))
  (menu-type [_] :fight-party-select)
  (key-pressed [_]))

(defn create-menu
  [state]
  (PlayerSelectMenu. state))
