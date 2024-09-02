(ns cljrpgengine.menus.shop-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menus.buy-menu :as buy-menu]))

(deftype ShopMenu [state shop]
  menu/Menu
  (draw [menu]
    (let [x (/ (first constants/window) 10)
          y (/ (second constants/window) 10)
          w (* x 8)
          h (* y 8)
          cursor (ui/get-menu-cursor state (.menu-type menu))]
      (ui/draw-window x y w h)
      (ui/draw-cursor x y (+ 2 cursor))
      (ui/draw-line x y 0 "Welcome to my shop!")
      (ui/draw-line x y 2 "Buy")
      (ui/draw-line x y 3 "Sell")
      (ui/draw-line x y 4 "Leave")))
  (cursor-length [_] 3)
  (menu-type [_] :shop)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))]
      (cond
        (= 0 cursor)
        (ui/open-menu! state (buy-menu/create-menu state shop))
        (= 1 cursor)
        (println "sell")
        (= 2 cursor)
        (ui/close-menu! state)))))

(defn create-menu
  [state shop]
  (ShopMenu. state shop))
