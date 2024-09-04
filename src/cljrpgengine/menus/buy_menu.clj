(ns cljrpgengine.menus.buy-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menus.confirm-buy-menu :as confirm-buy-menu]))

(deftype BuyMenu [state shop]
  menu/Menu
  (draw [menu]
    (let [x (/ (first constants/window) 8)
          y (/ (second constants/window) 8)
          w (* x 6)
          h (* y 5)
          cursor (ui/get-menu-cursor state (.menu-type menu))
          item-map (item/item-quantity-map (:items @state))]
      (ui/draw-window x y w h)
      (ui/draw-line
       x
       y
       0
       (str
        (ui/text-fixed-width "Name" constants/item-name-width)
        (ui/text-fixed-width "Cost" constants/cost-width)
        "Owned"))
      (let [items ((.shops (:scene @state)) shop)]
        (loop [i 0]
          (let [item (item/items (items i))]
            (ui/draw-line
             x
             y
             (+ i 2)
             (str
              (ui/text-fixed-width (:name item) constants/item-name-width)
              (ui/text-fixed-width (:worth item) constants/cost-width)
              ((items i) item-map))))
          (if (< i (dec (count items)))
            (recur (inc i)))))
      (ui/draw-cursor x y (+ 2 cursor))
      (ui/draw-window x (+ y h) w y)
      (ui/draw-line
       x
       (+ y h)
       0
       (str (ui/text-fixed-width "Cash on hand" constants/item-name-width) (:money @state)))))
  (cursor-length [_] (count ((.shops (:scene @state)) shop)))
  (menu-type [_] :buy)
  (key-pressed [menu]
    (ui/open-menu!
     state
     (confirm-buy-menu/create-menu
      state
      shop
      (((.shops (:scene @state)) shop) (ui/get-menu-cursor state (.menu-type menu)))))))

(defn create-menu
  [state shop]
  (BuyMenu. state shop))

