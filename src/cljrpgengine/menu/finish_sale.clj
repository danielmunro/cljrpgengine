(ns cljrpgengine.menu.finish-sale
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def window-padding 40)

(defn create
  [item-key quantity]
  (let [{:keys [name worth]} (get @item/items item-key)
        window (ui/create-window window-padding
                                 window-padding
                                 (- constants/screen-width (* 2 window-padding))
                                 (- constants/screen-height (* 2 window-padding)))]
    (doseq [_ (range 0 quantity)]
      (player/remove-item! item-key))
    (swap! player/gold (fn [amount] (+ amount (* worth quantity))))
    (.addActor window (ui/create-label (str "You sell " name (if (< 1 quantity)
                                                               (str " (x" quantity ")"))
                                            "\nGold gained: " (* worth quantity)
                                            "\nTotal gold: " @player/gold)
                                       constants/padding
                                       (ui/line-number window 3)))
    (menu/create-menu
     :finish-purchase
     window
     [(menu/create-option (ui/create-label "Ok" constants/left-cursor-padding (ui/line-number window 5))
                          (fn []
                            (menu/remove-menu! 3)))])))
