(ns cljrpgengine.menus.item-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(deftype ItemsMenu [state]
  menu/Menu
  (draw [menu]
    (let [y (/ (second constants/window) 10)
          cursor (ui/get-menu-cursor state (.menu-type menu))
          items (:items @state)
          item-count (count items)]
      (ui/draw-window 0 0 (first constants/window) (* 9 y))
      (ui/draw-cursor 0 0 (inc cursor))
      (ui/draw-line 0 0 0 (str (ui/text-fixed-width "Item" constants/item-name-width) " Quantity"))
      (loop [i 0]
        (if (< i item-count)
          (let [item (get items i)
                key (:key item)]
            (ui/draw-line
              0
              0
              (inc i)
              (str (ui/text-fixed-width (item/item-name key) constants/item-name-width) " " (:quantity item))
              (if (item/is-consumable? key)
                :font-default
                :font-disabled))
            (recur (inc i)))))
      (ui/draw-window 0 (* 9 y) (first constants/window) y)
      (ui/draw-line 0 (* 9 y) 0 (:description (item/items (:key (items cursor)))))))
  (cursor-length [_] (count (:items @state)))
  (menu-type [_] :items)
  (key-pressed [_]
    (println "item key pressed")))

(defn create-menu
  [state]
  (ItemsMenu. state))

