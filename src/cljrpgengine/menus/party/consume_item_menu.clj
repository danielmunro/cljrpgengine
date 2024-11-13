(ns cljrpgengine.menus.party.consume-item-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]))

(deftype ConsumeItemMenu [state item]
  menu/Menu
  (draw [menu]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (let [cursor (ui/get-menu-cursor (.menu-type menu))
          item-ref (get @item/items item)]
      (ui/draw-portraits @player/party item-ref cursor)
      (ui/draw-cursor
       10 (-> (* 80 cursor)
              (+ 20)))))
  (cursor-length [_] (count @player/party))
  (menu-type [_] :consume)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor (.menu-type menu))
          identifier (nth (keys @player/party) cursor)
          {:keys [affect amount]} (get @item/items item)
          {{:keys [hp max-hp mana max-mana]} identifier} @player/party]
      (item/remove-item! state item 1 :items)
      (cond
        (= :restore-hp affect)
        (swap! player/party update-in [identifier :hp] #(+ % (util/restore-amount amount hp max-hp)))
        (= :restore-mana)
        (swap! player/party update-in [identifier :mana] #(+ % (util/restore-amount amount mana max-mana))))
      (ui/close-menu!))))

(defn create
  [state item]
  (ConsumeItemMenu. state item))
