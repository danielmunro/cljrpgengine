(ns cljrpgengine.menus.shop.confirm-sell-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.shop.sale-complete-menu :as sale-complete-menu]))

(defn- complete-sale!
  [item-keyword quantity sale-price]
  (swap! player/player update-in [:gold] (fn [amount] (+ amount sale-price)))
  (player/remove-item! item-keyword quantity :sell))

(deftype ConfirmSellMenu [state item]
  menu/Menu
  (draw [menu]
    (let [x (/ (first constants/window) 10)
          y (/ (second constants/window) 10)
          w (* x 8)
          h (* y 8)
          cursor (ui/get-menu-cursor (.menu-type menu))]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 (str "Selling " (:name (get @item/items item))))
      (ui/draw-line x y 1 (str "Price " (* @ui/quantity (:worth (get @item/items item)))))
      (ui/draw-line x y 3 (str "Quantity " @ui/quantity))
      (ui/draw-line x y 4 "Yes")
      (ui/draw-line x y 5 "No")
      (ui/draw-cursor x y (+ cursor 4))))
  (cursor-length [_] 2)
  (menu-type [_] :confirm-sell)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor (.menu-type menu))]
      (cond
        (= 0 cursor)
        (do
          (complete-sale! item @ui/quantity (* (:worth (get @item/items item)) @ui/quantity))
          (ui/close-menu!)
          (ui/open-menu! (sale-complete-menu/create-menu state item @ui/quantity)))
        (= 1 cursor)
        (ui/close-menu!)))))

(defn create-menu
  [state item]
  (ui/reset-quantity! 1 (get (:items @player/player) item))
  (ConfirmSellMenu. state item))
