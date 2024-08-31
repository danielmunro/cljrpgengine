(ns cljrpgengine.menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.item :as item]
            [quil.core :as q]))

(defprotocol Menu
  (draw [menu state])
  (cursor-length [menu state])
  (menu-type [menu])
  (key-pressed [menu state]))

(deftype ItemsMenu []
  Menu
  (draw [_ state]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (ui/draw-cursor 5 (+ 12 (* (ui/get-last-menu-cursor state) 20)))
    (q/with-fill (:white constants/colors)
      (loop [i 0]
        (q/text (get-in item/items [(:name ((:items @state) i)) :name]) 20 (+ 25 (* i 20)))
        (if (< i (dec (count (:items @state))))
          (recur (inc i))))))
  (cursor-length [_ state] (count (:items @state)))
  (menu-type [_] :items)
  (key-pressed [_ state]
    (cond
      (= 0 (ui/get-last-menu-cursor state))
      (ui/open-menu! state))))

(defn create-items-menu
  []
  (ItemsMenu.))

(deftype PartyMenu []
  Menu
  (draw [_ state]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (let [x (* 3/4 (constants/window 0))]
      (ui/draw-cursor (- x 20) (-> (ui/get-last-menu-cursor state)
                                   (* 20)
                                   (+ 5)))
      (q/with-fill (:white constants/colors)
        (q/text "Items" x 20)
        (q/text "Magic" x 40)
        (q/text "Quests" x 60)
        (q/text "Save" x 80)
        (q/text "Quit" x 100))))
  (cursor-length [_ _] 5)
  (menu-type [_] :party)
  (key-pressed [_ state]
    (cond
      (= 0 (ui/get-last-menu-cursor state))
      (ui/open-menu! state (create-items-menu)))))

(defn create-party-menu
  []
  (PartyMenu.))
