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

(defprotocol Menu
  (draw [menu])
  (cursor-length [menu])
  (menu-type [menu])
  (key-pressed [menu]))

(deftype ItemsMenu [state]
  Menu
  (draw [menu]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (ui/draw-cursor 0 0 (ui/get-menu-cursor state (.menu-type menu)))
    (q/with-fill (:white constants/colors)
      (loop [i 0]
        (ui/draw-line 0 0 i (get-in item/items [(:name ((:items @state) i)) :name]))
        (if (< i (dec (count (:items @state))))
          (recur (inc i))))))
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

(deftype BuyMenu [state shop]
  Menu
  (draw [menu]
    (let [x (/ (first constants/window) 10)
          y (/ (second constants/window) 10)
          w (* x 8)
          h (* y 8)
          cursor (ui/get-menu-cursor state (.menu-type menu))]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 (str (text-fixed-width "Name" 35) "Cost"))
      (let [items ((.shops (:scene @state)) shop)]
        (loop [i 0]
          (let [item (item/items (items i))]
            (ui/draw-line x y (+ i 2) (str (text-fixed-width (:name item) 35) (:worth item))))
          (if (< i (dec (count items)))
            (recur (inc i)))))
      (ui/draw-cursor x y (+ 2 cursor))))
  (cursor-length [_] (count ((.shops (:scene @state)) shop)))
  (menu-type [_] :buy)
  (key-pressed [_]))

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
