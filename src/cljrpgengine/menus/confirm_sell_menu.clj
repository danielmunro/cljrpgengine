(ns cljrpgengine.menus.confirm-sell-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.sale-complete-menu :as sale-complete-menu]))

(defn- remove-item!
  [state item quantity]
  (loop [i 0]
    (if (= item (:name ((:items @state) i)))
      (dosync
       (alter state update-in [:items i :quantity] (fn [q] (- q quantity)))
       (if (= 0 (get-in @state [:items i :quantity]))
         (alter state assoc-in [:items] (into [] (filter #(< 0 (:quantity %)) (:items @state))))))
      (if (> (dec (count (:items @state))) i)
        (recur (inc i))))))

(defn- complete-sale!
  [state item-keyword quantity sale-price]
  (dosync (alter state update :money + sale-price))
  (remove-item! state item-keyword quantity))

(deftype ConfirmSellMenu [state item]
  menu/Menu
  (draw [menu]
    (let [x (/ (first constants/window) 10)
          y (/ (second constants/window) 10)
          w (* x 8)
          h (* y 8)
          cursor (ui/get-menu-cursor state (.menu-type menu))
          quantity (:quantity @state)]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 (str "Selling " (:name (item/items (:name item)))))
      (ui/draw-line x y 1 (str "Price " (* quantity (:worth (item/items (:name item))))))
      (ui/draw-line x y 3 (str "Quantity " quantity))
      (ui/draw-line x y 4 "Yes")
      (ui/draw-line x y 5 "No")
      (ui/draw-cursor x y (+ cursor 4))))
  (cursor-length [_] 2)
  (menu-type [_] :confirm-sell)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))
          quantity (:quantity @state)
          item-keyword (:name item)]
      (cond
        (= 0 cursor)
        (do
          (complete-sale! state item-keyword quantity (* (:worth (item/items item-keyword)) quantity))
          (ui/open-menu! state (sale-complete-menu/create-menu state item quantity)))
        (= 1 cursor)
        (ui/close-menu! state)))))

(defn create-menu
  [state item]
  (ui/reset-quantity! state 1 (:quantity item))
  (ConfirmSellMenu. state item))
