(ns cljrpgengine.menus.item-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(deftype ItemsMenu [state]
  menu/Menu
  (draw [menu]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (ui/draw-cursor 0 0 (inc (ui/get-menu-cursor state (.menu-type menu))))
    (ui/draw-line 0 0 0 (str (ui/text-fixed-width "Item" constants/item-name-width) " Quantity"))
    (loop [i 0]
      (let [item ((:items @state) i)]
        (ui/draw-line
         0
         0
         (inc i)
         (str (ui/text-fixed-width (get-in item/items [(:name item) :name]) constants/item-name-width) " " (:quantity item))
         (if (= :consumable (get-in item/items [(:name item) :type]))
           :font-default
           :font-disabled)))
      (if (< i (dec (count (:items @state))))
        (recur (inc i)))))
  (cursor-length [_] (count (:items @state)))
  (menu-type [_] :items)
  (key-pressed [_]
    (println "item key pressed")))

(defn create-menu
  [state]
  (ItemsMenu. state))

