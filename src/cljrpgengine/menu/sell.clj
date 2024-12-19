(ns cljrpgengine.menu.sell
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.select-quantity-to-sell :as select-quantity-to-sell-menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def window-padding 40)
(def item-name-width 30)

(defn create
  []
  (let [window (ui/create-window window-padding
                                 window-padding
                                 (- constants/screen-width (* 2 window-padding))
                                 (- constants/screen-height (* 2 window-padding)))
        i (atom 3)]
    (.addActor window (ui/create-label "What would you like to sell?"
                                       constants/padding
                                       (ui/line-number window 1)))
    (.addActor window (ui/create-label (str (ui/text-fixed-width "Item" item-name-width)
                                            "Price")
                                       constants/padding
                                       (ui/line-number window 2)))
    (menu/create-menu-2
     :sell
     window
     (mapv (fn [item-key]
             (let [{:keys [name worth]} (get @item/items item-key)]
               (menu/create-option
                (ui/create-label (str (ui/text-fixed-width
                                       name
                                       item-name-width)
                                      worth)
                                 constants/left-cursor-padding
                                 (ui/line-number window (swap! i inc)))
                #(menu/add-menu! (select-quantity-to-sell-menu/create item-key)))))
           (keys @player/items)))))
