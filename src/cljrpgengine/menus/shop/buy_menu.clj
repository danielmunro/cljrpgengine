(ns cljrpgengine.menus.shop.buy-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.player :as player]
            [cljrpgengine.shop :as shop]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menus.shop.confirm-buy-menu :as confirm-buy-menu]))

(deftype BuyMenu [shop]
  menu/Menu
  (draw [menu]
    (let [x (/ constants/screen-width 8)
          y (/ constants/screen-height 8)
          w (* x 6)
          h (* y 5)
          cursor (ui/get-menu-cursor (.menu-type menu))
          item-map (item/item-quantity-map (:items @player/player))
          items (get @shop/shops shop)
          item-count (count items)]
      (ui/draw-window x y w h)
      (ui/draw-line
       x
       y
       0
       (str
        (ui/text-fixed-width "Name" constants/item-name-width)
        (ui/text-fixed-width "Cost" constants/cost-width)
        "Owned"))
      (loop [i 0]
        (if (< i item-count)
          (let [item (get @item/items (get items i))]
            (ui/draw-line
             x
             y
             (+ i 2)
             (str
              (ui/text-fixed-width (:name item) constants/item-name-width)
              (ui/text-fixed-width (:worth item) constants/cost-width)
              (get item-map (get items i))))
            (recur (inc i)))))
      (ui/draw-cursor x y (+ 2 cursor))
      (ui/draw-window x (+ y h) w y)
      (ui/draw-line
       x
       (+ y h)
       0
       (str (ui/text-fixed-width "Gold" constants/item-name-width) (:gold @player/player)))))
  (cursor-length [_] (count (get @shop/shops shop)))
  (menu-type [_] :buy)
  (key-pressed [menu]
    (ui/open-menu!
     (confirm-buy-menu/create-menu
      shop
      ((get @shop/shops shop) (ui/get-menu-cursor (.menu-type menu)))))))

(defn create-menu
  [shop]
  (BuyMenu. shop))

