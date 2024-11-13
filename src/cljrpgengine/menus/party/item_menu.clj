(ns cljrpgengine.menus.party.item-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menus.party.consume-item-menu :as consume-item-menu]
            [cljrpgengine.ui :as ui]))

(deftype ItemsMenu [state]
  menu/Menu
  (draw [menu]
    (let [y (/ (second constants/window) 10)
          cursor (ui/get-menu-cursor (.menu-type menu))
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
              (fn [key]
                (fn [line-number]
                  (let [item-ref (get @item/items key)]
                    (ui/draw-line 0 0 line-number
                                  (str
                                   (ui/text-fixed-width
                                    (:name item-ref)
                                    constants/item-name-width) " " (get items key))
                                  (if (= :consumable (:type item-ref))
                                    :font-default
                                    :font-disabled))))) (keys items))))
      (ui/draw-window 0 (* 9 y) (first constants/window) y)
      (if-let [item (get @item/items (item/get-item-at-inventory-index items cursor))]
        (ui/draw-line 0 (* 9 y) 0 (:description item)))))
  (cursor-length [_] (count (:items @state)))
  (menu-type [_] :items)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor (.menu_type menu))
          items (:items @state)
          item-selected (item/get-item-at-inventory-index items cursor)
          item (get @item/items item-selected)]
      (when (= :consumable (:type item))
        (ui/open-menu! (consume-item-menu/create state item-selected))))))

(defn create-menu
  [state]
  (ItemsMenu. state))

