(ns cljrpgengine.menu.select-quantity-to-sell
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.finish-sale :as finish-sale-menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def window-padding 40)
(def x-padding 30)

(defn create
  [item-key]
  (let [{:keys [name worth]} (get @item/items item-key)
        owned (get @player/items item-key)
        window (ui/create-window window-padding
                                 window-padding
                                 (- constants/screen-width (* 2 window-padding))
                                 (- constants/screen-height (* 2 window-padding)))
        quantity (atom 1)
        quantity-label (ui/create-label (str @quantity " of " (get @player/items item-key))
                                        constants/padding
                                        (ui/line-number window 4))
        sale-gold-label (ui/create-label (str "Gold to gain: " (* @quantity worth))
                                         constants/padding
                                         (ui/line-number window 5))]
    (.addActor window (ui/create-label (str "Sell " name ":\n(Use 'left' and 'right' to adjust the quantity)")
                                       constants/padding
                                       (ui/line-number window 2)))
    (.addActor window quantity-label)
    (.addActor window sale-gold-label)
    (menu/create-menu
     :quantity-to-sell
     window
     [(menu/create-option
       (ui/create-label "Sell now"
                        x-padding
                        (ui/line-number window 7))
       #(menu/add-menu! (finish-sale-menu/create item-key @quantity)))
      (menu/create-option
       (ui/create-label "Never mind"
                        x-padding
                        (ui/line-number window 8))
       #(menu/remove-menu!))]
     (fn [changed]
       (case changed
         :left
         (if (< 1 @quantity)
           (do (swap! quantity dec)
               (.setText quantity-label (str @quantity " of " owned))
               (.setText sale-gold-label (str "Gold to gain: " (* @quantity worth)))))
         :right
         (if (<= (inc @quantity) owned)
           (do (swap! quantity inc)
               (.setText quantity-label (str @quantity " of " owned))
               (.setText sale-gold-label (str "Gold to gain: " (* @quantity worth)))))
         false)))))
