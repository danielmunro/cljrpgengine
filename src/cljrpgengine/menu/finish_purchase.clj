(ns cljrpgengine.menu.finish-purchase
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def window-padding 40)

(defn create
  [item quantity]
  (doseq [_ (range 0 quantity)]
    (player/add-item! (:identifier item)))
  (swap! player/gold (fn [amount] (- amount (* (:worth item) quantity))))
  (let [window (ui/create-window window-padding
                                 window-padding
                                 (- constants/screen-width (* 2 window-padding))
                                 (- constants/screen-height (* 2 window-padding)))]
    (.addActor window (ui/create-label (str "You purchased " (:name item) (if (< 1 quantity)
                                                                            (str " (x" quantity ")"))
                                            "\nCost: " (* (:worth item) quantity) " gold")
                                       constants/padding
                                       (ui/line-number window 2)))
    (.addActor window (ui/create-label (str "Gold remaining: " @player/gold)
                                       constants/padding
                                       (ui/line-number window 3)))
    (menu/create-menu
     :finish-purchase
     window
     [(menu/create-option (ui/create-label "Ok" constants/left-cursor-padding (ui/line-number window 5))
                          (fn []
                            (menu/remove-menu! 3)))])))
