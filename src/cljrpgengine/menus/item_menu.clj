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
          height (* 9 y)
          max-lines-on-screen (-> height
                                  (- constants/padding)
                                  (- (* 2 constants/line-spacing))
                                  (/ constants/line-spacing)
                                  (Math/floor))]
      (ui/draw-window 0 0 (first constants/window) height)
      (ui/draw-line 0 0 0 (str (ui/text-fixed-width "Item" constants/item-name-width) " Quantity"))
      (ui/scrollable-area
       0 0
       cursor
       max-lines-on-screen
       1
       (into []
             (map #(str
                    (ui/text-fixed-width (item/item-name (:key %)) constants/item-name-width) " " (:quantity %))) items))
      (ui/draw-window 0 (* 9 y) (first constants/window) y)
      (ui/draw-line 0 (* 9 y) 0 (:description (item/items (:key (items cursor)))))))
  (cursor-length [_] (count (:items @state)))
  (menu-type [_] :items)
  (key-pressed [_]
    (println "item key pressed")))

(defn create-menu
  [state]
  (ItemsMenu. state))

