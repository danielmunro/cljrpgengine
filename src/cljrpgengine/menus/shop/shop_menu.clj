(ns cljrpgengine.menus.shop.shop-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menus.shop.buy-menu :as buy-menu]
            [cljrpgengine.menus.shop.sell-menu :as sell-menu]))

(deftype ShopMenu [shop]
  menu/Menu
  (draw [menu]
    (let [x (/ (first constants/window) 8)
          y (/ (second constants/window) 8)
          w (* x 6)
          h (* y 6)
          cursor (ui/get-menu-cursor (.menu-type menu))]
      (ui/draw-window x y w h)
      (ui/draw-cursor x y (+ 2 cursor))
      (ui/draw-line x y 0 "Welcome to my shop!")
      (ui/draw-line x y 2 "Buy")
      (ui/draw-line x y 3 "Sell")
      (ui/draw-line x y 4 "Leave")))
  (cursor-length [_] 3)
  (menu-type [_] :shop)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor (.menu-type menu))]
      (cond
        (= 0 cursor)
        (ui/open-menu! (buy-menu/create-menu shop))
        (= 1 cursor)
        (ui/open-menu! (sell-menu/create-menu))
        (= 2 cursor)
        (ui/close-menu!)))))

(defn create-menu
  [shop]
  (ShopMenu. shop))
