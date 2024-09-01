(ns cljrpgengine.menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.item :as item]
            [quil.core :as q]))

(defprotocol Menu
  (draw [menu])
  (cursor-length [menu])
  (cursor-orientation [menu])
  (menu-type [menu])
  (key-pressed [menu]))

(deftype ItemsMenu [state]
  Menu
  (draw [_]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (ui/draw-cursor 5 (+ 12 (* (ui/get-menu-cursor state :items) 20)))
    (q/with-fill (:white constants/colors)
      (loop [i 0]
        (q/text (get-in item/items [(:name ((:items @state) i)) :name]) 20 (+ 25 (* i 20)))
        (if (< i (dec (count (:items @state))))
          (recur (inc i))))))
  (cursor-length [_] (count (:items @state)))
  (cursor-orientation [_] :vertical)
  (menu-type [_] :items)
  (key-pressed [_]
    (cond
      (= 0 (ui/get-menu-cursor state :items))
      (ui/open-menu! state))))

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
      (q/with-fill (:white constants/colors)
        (q/text "Are you sure?" (+ x 30) (+ y 30))
        (q/text "No" (+ x 30) (+ y 50))
        (q/text "Yes" (+ x 90) (+ y 50)))
      (cond
        (= 0 cursor)
        (ui/draw-cursor (+ x 10) (+ y 36))
        (= 1 cursor)
        (ui/draw-cursor (+ x 70) (+ y 36)))))
  (cursor-length [_] 2)
  (cursor-orientation [_] :horizontal)
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
  (draw [_]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (let [cursor (ui/get-menu-cursor state :party)
          x (* 3/4 (first constants/window))]
      (ui/draw-cursor (- x 20) (-> cursor
                                   (* 20)
                                   (+ 5)))
      (q/with-fill (:white constants/colors)
        (q/text "Items" x 20)
        (q/text "Magic" x 40)
        (q/text "Quests" x 60)
        (q/text "Save" x 80)
        (q/text "Quit" x 100))))
  (cursor-length [_] 5)
  (cursor-orientation [_] :vertical)
  (menu-type [_] :party)
  (key-pressed [_]
    (let [cursor (ui/get-menu-cursor state :party)]
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
  (draw [_]
    (let [x (/ (first constants/window) 10)
          y (/ (second constants/window) 10)
          w (* x 8)
          h (* y 8)
          cursor (ui/get-menu-cursor state :buy)]
      (ui/draw-window x y w h)
      (q/with-fill (:white constants/colors)
        (let [items ((.shops (:scene @state)) shop)]
          (loop [i 0]
            (q/text (:name (item/items (items i))) (+ x 30) (+ (+ y (* i 20)) 25))
            (if (< i (dec (count items)))
              (recur (inc i))))))
      (ui/draw-cursor (+ x 10) (+ (* cursor 20) 12 y))))
  (cursor-length [_] (count ((.shops (:scene @state)) shop)))
  (cursor-orientation [_] :vertical)
  (menu-type [_] :buy)
  (key-pressed [_]))

(defn create-buy-menu
  [state shop]
  (BuyMenu. state shop))

(deftype ShopMenu [state shop]
  Menu
  (draw [_]
    (let [x (/ (first constants/window) 10)
          y (/ (second constants/window) 10)
          w (* x 8)
          h (* y 8)
          cursor (ui/get-menu-cursor state :shop)]
      (ui/draw-window x y w h)
      (q/with-fill (:white constants/colors)
        (q/text "Buy" (+ x 30) (+ y 30))
        (q/text "Sell" (+ x 30) (+ y 50))
        (q/text "Leave" (+ x 30) (+ y 70)))
      (ui/draw-cursor (+ x 10) (-> cursor
                                   (* 20)
                                   (+ y)
                                   (+ 15)))))
  (cursor-length [_] 3)
  (cursor-orientation [_] :vertical)
  (menu-type [_] :shop)
  (key-pressed [_]
    (let [cursor (ui/get-menu-cursor state :shop)]
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
