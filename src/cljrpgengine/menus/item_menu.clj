(ns cljrpgengine.menus.item-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menus.consume-item-menu :as consume-item-menu]
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
             (map
              (fn [item]
                (fn [line-number]
                  (ui/draw-line 0 0 line-number
                                (str
                                 (ui/text-fixed-width
                                  (:name (get item/items (:key item)))
                                  constants/item-name-width) " " (:quantity item))
                                (if (= :consumable (:type (item/items (:key item))))
                                  :font-default
                                  :font-disabled))))) items))
      (ui/draw-window 0 (* 9 y) (first constants/window) y)
      (if-let [item-ref (get items cursor)]
        (ui/draw-line 0 (* 9 y) 0 (:description (item/items (:key item-ref)))))))
  (cursor-length [_] (count (:items @state)))
  (menu-type [_] :items)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor state (.menu_type menu))
          items (:items @state)
          item-selected (get items cursor)
          item (item/items (:key item-selected))]
      (when (= :consumable (:type item))
        (ui/open-menu! state (consume-item-menu/create state item-selected))))))

(defn create-menu
  [state]
  (ItemsMenu. state))

