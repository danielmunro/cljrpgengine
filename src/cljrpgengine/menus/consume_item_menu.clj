(ns cljrpgengine.menus.consume-item-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]))

(deftype ConsumeItemMenu [state item]
  menu/Menu
  (draw [menu]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))
          item-ref (item/items (:key item))]
      (ui/draw-portraits state item-ref cursor)
      (ui/draw-cursor
        10 (-> (* 80 cursor)
               (+ 20)))))
  (cursor-length [_] (count (get-in @state [:player :party])))
  (menu-type [_] :consume)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))
          {:keys [affect amount]} (item/items (:key item))
          {{{{:keys [hp max-hp mana max-mana]} cursor} :party} :player} @state]
      (item/remove-item! state (:key item) 1 :items)
      (cond
        (= :restore-hp affect)
        (dosync (alter state update-in [:player :party cursor :hp] #(+ % (util/restore-amount amount hp max-hp))))
        (= :restore-mana)
        (dosync (alter state update-in [:player :party cursor :mana] #(+ % (util/restore-amount amount mana max-mana)))))
      (ui/close-menu! state))))

(defn create
  [state item]
  (ConsumeItemMenu. state item))
