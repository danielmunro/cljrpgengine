(ns cljrpgengine.menu.shop
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.buy :as buy-menu]
            [cljrpgengine.menu.sell :as sell-menu]
            [cljrpgengine.ui :as ui]))

(def window-padding 40)

(defn create
  [shop]
  (let [window (ui/create-window window-padding
                                 window-padding
                                 (- constants/screen-width (* 2 window-padding))
                                 (- constants/screen-height (* 2 window-padding)))]
    (.addActor window (ui/create-label "Hello! What would you like to do today?"
                                       constants/padding (ui/line-number window 1)))
    (menu/create-menu-2
     :shop
     window
     [(menu/create-option
       (ui/create-label "Buy" constants/left-cursor-padding (ui/line-number window 3))
       #(menu/add-menu! (buy-menu/create shop)))

      (menu/create-option
       (ui/create-label "Sell" constants/left-cursor-padding (ui/line-number window 4))
       #(menu/add-menu! (sell-menu/create)))

      (menu/create-option
       (ui/create-label "Goodbye" constants/left-cursor-padding (ui/line-number window 5))
       #(menu/remove-menu!))])))
