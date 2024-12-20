(ns cljrpgengine.menus.shop.confirm-buy-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.shop.purchase-complete-menu :as purchase-complete-menu]))

(defn- complete-purchase!
  [item quantity purchase-price]
  (swap! player/player update-in [:gold] (fn [amount] (- amount purchase-price)))
  (player/add-item! item quantity))

(deftype ConfirmBuyMenu [shop item]
  menu/Menu
  (draw [menu]
    (let [x (/ (first constants/window) 5)
          y (/ (second constants/window) 5)
          w (* x 3)
          h (* y 3)
          cursor (ui/get-menu-cursor (.menu-type menu))]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 (str "Purchasing " (:name (get @item/items item))))
      (ui/draw-line x y 1 (str "Cost " (* @ui/quantity (:worth (get @item/items item)))))
      (ui/draw-line x y 3 (str "Quantity " @ui/quantity))
      (ui/draw-line x y 4 "Yes")
      (ui/draw-line x y 5 "No")
      (ui/draw-cursor x y (+ cursor 3))))
  (cursor-length [_] 3)
  (menu-type [_] :confirm-buy)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor (.menu-type menu))]
      (cond
        (= 1 cursor)
        (do
          (complete-purchase! item @ui/quantity (* (:worth (get @item/items item)) @ui/quantity))
          (ui/close-menu!)
          (ui/open-menu! (purchase-complete-menu/create-menu shop item @ui/quantity)))
        (= 2 cursor)
        (ui/close-menu!)))))

(defn create-menu
  [shop item]
  (ui/reset-quantity! 1 (Math/floor (/ (:gold @player/player) (:worth (get @item/items item)))))
  (ConfirmBuyMenu. shop item))
