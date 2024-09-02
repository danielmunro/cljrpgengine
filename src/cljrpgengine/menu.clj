(ns cljrpgengine.menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.item :as item]
            [quil.core :as q]))

(defn- text-fixed-width
  [text spaces]
  (if (> (count text) spaces)
    (str (subs text 0 (- spaces 4)) "... ")
    (loop [t text]
      (if (> spaces (count t))
        (recur (str t " "))
        t))))

(defn add-item!
  [state item quantity]
  (let [added (atom false)]
    (loop [i 0]
      (if (= item (:name ((:items @state) i)))
        (do
          (dosync (alter state update-in [:items i :quantity] (fn [q] (+ quantity q))))
          (swap! added (constantly true)))
        (if (> (dec (count (:items @state))) i)
          (recur (inc i)))))
    (if (not @added)
      (dosync (alter state update :items conj {:name item :quantity quantity})))))

(defn- complete-purchase!
  [state item-keyword quantity purchase-price]
  (dosync
   (alter state update :money - purchase-price))
  (add-item! state item-keyword quantity))

(defn- reset-quantity!
  [state min max]
  (dosync
   (alter state assoc :quantity 1 :quantity-min min :quantity-max max)))

(defprotocol Menu
  (draw [menu])
  (cursor-length [menu])
  (menu-type [menu])
  (key-pressed [menu]))

(deftype ItemsMenu [state]
  Menu
  (draw [menu]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (ui/draw-cursor 0 0 (inc (ui/get-menu-cursor state (.menu-type menu))))
    (ui/draw-line 0 0 0 (str (text-fixed-width "Item" constants/item-name-width) " Quantity"))
    (loop [i 0]
      (let [item ((:items @state) i)]
        (ui/draw-line 0 0 (inc i) (str (text-fixed-width (get-in item/items [(:name item) :name]) constants/item-name-width) " " (:quantity item))))
      (if (< i (dec (count (:items @state))))
        (recur (inc i)))))
  (cursor-length [_] (count (:items @state)))
  (menu-type [_] :items)
  (key-pressed [_]
    (println "item key pressed")))

(defn create-items-menu
  [state]
  (ItemsMenu. state))

(deftype QuitMenu [state]
  Menu
  (draw [_]
    (let [w (/ (first constants/window) 2)
          h (/ (second constants/window) 2)
          x (/ w 2)
          y (/ h 2)
          cursor (ui/get-menu-cursor state :quit)]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 "Are you sure?")
      (ui/draw-line x y 2 "No")
      (ui/draw-line x y 3 "Yes")
      (ui/draw-cursor x y (if (= cursor 0) 2 3))))
  (cursor-length [_] 2)
  (menu-type [_] :quit)
  (key-pressed [_]
    (let [cursor (ui/get-menu-cursor state :quit)]
      (cond
        (= 0 cursor)
        (ui/close-menu! state)
        (= 1 cursor)
        (System/exit 0)))))

(defn create-quit-menu
  [state]
  (QuitMenu. state))

(deftype PartyMenu [state]
  Menu
  (draw [menu]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))
          x (* 3/4 (first constants/window))]
      (ui/draw-cursor x 0 cursor)
      (ui/draw-line x 0 0 "Items")
      (ui/draw-line x 0 1 "Magic")
      (ui/draw-line x 0 2 "Quests")
      (ui/draw-line x 0 3 "Save")
      (ui/draw-line x 0 4 "Quit")))
  (cursor-length [_] 5)
  (menu-type [_] :party)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))]
      (cond
        (= 0 cursor)
        (ui/open-menu! state (create-items-menu state))
        (= 1 cursor)
        (println "magic")
        (= 2 cursor)
        (println "quests")
        (= 3 cursor)
        (println "save")
        (= 4 cursor)
        (ui/open-menu! state (create-quit-menu state))))))

(defn create-party-menu
  [state]
  (PartyMenu. state))

(deftype PurchaseCompleteMenu [state shop item quantity]
  Menu
  (draw [_]
    (let [x (/ (first constants/window) 5)
          y (/ (second constants/window) 5)
          w (* x 3)
          h (* y 3)]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 "Purchase complete!")))
  (cursor-length [_] 0)
  (menu-type [_] :purchase-complete)
  (key-pressed [_]
    (ui/close-menu! state)
    (ui/close-menu! state)))

(defn create-purchase-complete-menu
  [state shop item quantity]
  (PurchaseCompleteMenu. state shop item quantity))

(deftype ConfirmBuyMenu [state shop item]
  Menu
  (draw [menu]
    (let [x (/ (first constants/window) 5)
          y (/ (second constants/window) 5)
          w (* x 3)
          h (* y 3)
          cursor (ui/get-menu-cursor state (.menu-type menu))
          quantity (:quantity @state)]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 (str "Purchasing " (:name (item/items item))))
      (ui/draw-line x y 1 (str "Cost " (* quantity (:worth (item/items item)))))
      (ui/draw-line x y 3 (str "Quantity " quantity))
      (ui/draw-line x y 4 "Yes")
      (ui/draw-line x y 5 "No")
      (ui/draw-cursor x y (+ cursor 3))))
  (cursor-length [_] 3)
  (menu-type [_] :confirm-buy)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))
          quantity (:quantity @state)]
      (cond
        (= 1 cursor)
        (do
          (complete-purchase! state item quantity (* (:worth (item/items item)) quantity))
          (ui/open-menu! state (create-purchase-complete-menu state shop item quantity)))
        (= 2 cursor)
        (ui/close-menu! state)))))

(defn create-confirm-buy-menu
  [state shop item]
  (reset-quantity! state 1 (Math/floor (/ (:money @state) (:worth (item/items item)))))
  (ConfirmBuyMenu. state shop item))

(deftype BuyMenu [state shop]
  Menu
  (draw [menu]
    (let [x (/ (first constants/window) 10)
          y (/ (second constants/window) 10)
          w (* x 8)
          h (* y 8)
          cursor (ui/get-menu-cursor state (.menu-type menu))]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 (str (text-fixed-width "Name" constants/item-name-width) "Cost"))
      (let [items ((.shops (:scene @state)) shop)]
        (loop [i 0]
          (let [item (item/items (items i))]
            (ui/draw-line x y (+ i 2) (str (text-fixed-width (:name item) constants/item-name-width) (:worth item))))
          (if (< i (dec (count items)))
            (recur (inc i)))))
      (ui/draw-cursor x y (+ 2 cursor))))
  (cursor-length [_] (count ((.shops (:scene @state)) shop)))
  (menu-type [_] :buy)
  (key-pressed [menu]
    (ui/open-menu!
     state
     (create-confirm-buy-menu
      state
      shop
      (((.shops (:scene @state)) shop) (ui/get-menu-cursor state (.menu-type menu)))))))

(defn create-buy-menu
  [state shop]
  (BuyMenu. state shop))

(deftype ShopMenu [state shop]
  Menu
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
        (ui/open-menu! state (create-buy-menu state shop))
        (= 1 cursor)
        (println "sell")
        (= 2 cursor)
        (ui/close-menu! state)))))

(defn create-shop-menu
  [state shop]
  (ShopMenu. state shop))
