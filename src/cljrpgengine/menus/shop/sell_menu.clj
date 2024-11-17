(ns cljrpgengine.menus.shop.sell-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.shop.confirm-sell-menu :as confirm-sell-menu]))

(deftype SellMenu [state]
  menu/Menu
  (draw [menu]
    (let [x (/ (first constants/window) 10)
          y (/ (second constants/window) 10)
          w (* x 8)
          h (* y 8)
          cursor (ui/get-menu-cursor (.menu-type menu))
          items (:items @player/player)]
      (ui/draw-window x y w h)
      (ui/draw-line
       x
       y
       0
       (str
        (ui/text-fixed-width "Name" constants/item-name-width)
        (ui/text-fixed-width "Price" constants/cost-width)
        "Owned"))
      (loop [i 0]
        (if (< i (count items))
          (do
            (let [item (get @item/items (nth (keys items) i))]
              (if (not (:special item))
                (ui/draw-line
                 x
                 y
                 (+ i 2)
                 (str
                  (ui/text-fixed-width (:name item) constants/item-name-width)
                  (ui/text-fixed-width (int (Math/floor (/ (or (:worth item) 0) 2))) constants/cost-width)
                  (get items (nth (keys items) i))))))
            (recur (inc i)))))
      (ui/draw-cursor x y (+ cursor 2))))
  (cursor-length [_] (count (:items @player/player)))
  (menu-type [_] :sell)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor (.menu-type menu))]
      (if-let [item (nth (keys (:items @player/player)) cursor)]
        (ui/open-menu! (confirm-sell-menu/create-menu state item))))))

(defn create-menu
  [state]
  (SellMenu. state))
