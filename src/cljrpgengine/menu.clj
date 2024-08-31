(ns cljrpgengine.menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.item :as item]
            [quil.core :as q]))

(defprotocol Menu
  (draw [menu state])
  (cursor-length [menu state])
  (cursor-orientation [menu])
  (menu-type [menu])
  (key-pressed [menu state]))

(deftype ItemsMenu []
  Menu
  (draw [_ state]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (ui/draw-cursor 5 (+ 12 (* (ui/get-menu-cursor state :items) 20)))
    (q/with-fill (:white constants/colors)
      (loop [i 0]
        (q/text (get-in item/items [(:name ((:items @state) i)) :name]) 20 (+ 25 (* i 20)))
        (if (< i (dec (count (:items @state))))
          (recur (inc i))))))
  (cursor-length [_ state] (count (:items @state)))
  (cursor-orientation [_] :vertical)
  (menu-type [_] :items)
  (key-pressed [_ state]
    (cond
      (= 0 (ui/get-menu-cursor state :items))
      (ui/open-menu! state))))

(defn create-items-menu
  []
  (ItemsMenu.))

(deftype QuitMenu []
  Menu
  (draw [_ state]
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
  (cursor-length [_ _] 2)
  (cursor-orientation [_] :horizontal)
  (menu-type [_] :quit)
  (key-pressed [_ state]
    (let [cursor (ui/get-menu-cursor state :quit)]
      (cond
        (= 0 cursor)
        (ui/close-menu! state)
        (= 1 cursor)
        (System/exit 0)))))

(defn create-quit-menu
  []
  (QuitMenu.))

(deftype PartyMenu []
  Menu
  (draw [_ state]
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
  (cursor-length [_ _] 5)
  (cursor-orientation [_] :vertical)
  (menu-type [_] :party)
  (key-pressed [_ state]
    (let [cursor (ui/get-menu-cursor state :party)]
      (cond
        (= 0 cursor)
        (ui/open-menu! state (create-items-menu))
        (= 4 cursor)
        (ui/open-menu! state (create-quit-menu))))))

(defn create-party-menu
  []
  (PartyMenu.))
