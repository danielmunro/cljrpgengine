(ns cljrpgengine.menus.shop.confirm-sell-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.shop.sale-complete-menu :as sale-complete-menu]))

(defn- complete-sale!
  [state item-keyword quantity sale-price]
  (dosync (alter state update :money + sale-price))
  (item/remove-item! state item-keyword quantity :sell))

(deftype ConfirmSellMenu [state item]
  menu/Menu
  (draw [menu]
    (let [x (/ (first constants/window) 10)
          y (/ (second constants/window) 10)
          w (* x 8)
          h (* y 8)
          cursor (ui/get-menu-cursor (.menu-type menu))
          quantity (:quantity @state)]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 (str "Selling " (:name (get @item/items item))))
      (ui/draw-line x y 1 (str "Price " (* quantity (:worth (get @item/items item)))))
      (ui/draw-line x y 3 (str "Quantity " quantity))
      (ui/draw-line x y 4 "Yes")
      (ui/draw-line x y 5 "No")
      (ui/draw-cursor x y (+ cursor 4))))
  (cursor-length [_] 2)
  (menu-type [_] :confirm-sell)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor (.menu-type menu))
          quantity (:quantity @state)]
      (cond
        (= 0 cursor)
        (do
          (complete-sale! state item quantity (* (:worth (get @item/items item)) quantity))
          (ui/close-menu!)
          (ui/open-menu! (sale-complete-menu/create-menu state item quantity)))
        (= 1 cursor)
        (ui/close-menu!)))))

(defn create-menu
  [state item]
  (ui/reset-quantity! state 1 (:quantity item))
  (ConfirmSellMenu. state item))
