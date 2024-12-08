(ns cljrpgengine.menu.buy
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.select-quantity-to-buy :as select-quantity-to-buy-menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def window-padding 40)
(def x-padding 30)
(def item-name-width 30)

(defn create
  [shop]
  (let [window (ui/create-window window-padding
                                 window-padding
                                 (- constants/screen-width (* 2 window-padding))
                                 (- constants/screen-height (* 2 window-padding)))
        items (map #(get @item/items %) shop)
        i (atom 4)]
    (.addActor window (ui/create-label "Here's what I have for sale:"
                                       constants/padding
                                       (ui/line-number window 1)))
    (.addActor window (ui/create-label (str (ui/text-fixed-width "Item" item-name-width) "Price")
                                       x-padding
                                       (ui/line-number window 3)))
    (menu/create-menu
     :buy
     window
     (mapv (fn [item]
             (menu/create-option
              (ui/create-label (str (ui/text-fixed-width (:name item) item-name-width) (:worth item))
                               x-padding
                               (ui/line-number window (swap! i inc))
                               (if (<= (:worth item) @player/gold)
                                 (:default constants/font-colors)
                                 (:disabled constants/font-colors)))
              (fn []
                (if (<= (:worth item) @player/gold)
                  (menu/add-menu! (select-quantity-to-buy-menu/create item))))))
           items))))
