(ns cljrpgengine.menu.select-quantity-to-buy
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.finish-purchase :as finish-purchase-menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def window-padding 40)

(defn create
  [item]
  (let [quantity (atom 1)
        window (ui/create-window window-padding
                                 window-padding
                                 (- constants/screen-width (* 2 window-padding))
                                 (- constants/screen-height (* 2 window-padding)))
        quantity-label (ui/create-label (str "Quantity: " @quantity)
                                        constants/padding
                                        (ui/line-number window 4))
        price-label (ui/create-label (str "Price: " (* @quantity (:worth item)))
                                     constants/padding
                                     (ui/line-number window 5))
        gold-remaining-label (ui/create-label (str "Gold remaining: " (- @player/gold (* @quantity (:worth item))))
                                              constants/padding
                                              (ui/line-number window 6))]
    (.addActor window (ui/create-label "How many would you like to buy?"
                                       constants/padding
                                       (ui/line-number window 1)))
    (.addActor window (ui/create-label "(Use 'left' and 'right' to adjust the quantity)"
                                       constants/padding
                                       (ui/line-number window 2)))
    (.addActor window quantity-label)
    (.addActor window price-label)
    (.addActor window gold-remaining-label)
    (menu/create-menu
     :quantity-to-buy
     window
     [(menu/create-option
       (ui/create-label "Purchase" constants/left-cursor-padding (ui/line-number window 8))
       #(menu/add-menu! (finish-purchase-menu/create item @quantity)))
      (menu/create-option
       (ui/create-label "Never mind" constants/left-cursor-padding (ui/line-number window 9))
       #(menu/remove-menu!))]
     (fn [changed]
       (case changed
         :left
         (if (< 1 @quantity)
           (do (swap! quantity dec)
               (.setText quantity-label (str "Quantity: " @quantity))
               (.setText price-label (str "Price: " (* @quantity (:worth item))))
               (.setText gold-remaining-label (str "Gold remaining: " (- @player/gold (* @quantity (:worth item)))))))
         :right
         (if (<= (* (inc @quantity) (:worth item)) @player/gold)
           (do (swap! quantity inc)
               (.setText quantity-label (str "Quantity: " @quantity))
               (.setText price-label (str "Price: " (* @quantity (:worth item))))
               (.setText gold-remaining-label (str "Gold remaining: " (- @player/gold (* @quantity (:worth item)))))))
         false)))))