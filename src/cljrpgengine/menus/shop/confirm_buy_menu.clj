(ns cljrpgengine.menus.shop.confirm-buy-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.shop.purchase-complete-menu :as purchase-complete-menu]))

(defn- complete-purchase!
  [state item quantity purchase-price]
  (dosync
   (alter state update :money - purchase-price))
  (player/add-item! item quantity))

(deftype ConfirmBuyMenu [state shop item]
  menu/Menu
  (draw [menu]
    (let [x (/ (first constants/window) 5)
          y (/ (second constants/window) 5)
          w (* x 3)
          h (* y 3)
          cursor (ui/get-menu-cursor (.menu-type menu))
          quantity (:quantity @state)]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 (str "Purchasing " (:name (get @item/items item))))
      (ui/draw-line x y 1 (str "Cost " (* quantity (:worth (get @item/items item)))))
      (ui/draw-line x y 3 (str "Quantity " quantity))
      (ui/draw-line x y 4 "Yes")
      (ui/draw-line x y 5 "No")
      (ui/draw-cursor x y (+ cursor 3))))
  (cursor-length [_] 3)
  (menu-type [_] :confirm-buy)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor (.menu-type menu))
          quantity (:quantity @state)]
      (cond
        (= 1 cursor)
        (do
          (complete-purchase! state item quantity (* (:worth (get @item/items item)) quantity))
          (ui/close-menu!)
          (ui/open-menu! (purchase-complete-menu/create-menu state shop item quantity)))
        (= 2 cursor)
        (ui/close-menu!)))))

(defn create-menu
  [state shop item]
  (ui/reset-quantity! state 1 (Math/floor (/ (:money @state) (:worth (get @item/items item)))))
  (ConfirmBuyMenu. state shop item))
