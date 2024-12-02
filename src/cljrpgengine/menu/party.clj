(ns cljrpgengine.menu.party
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(def from-right 120)

(defn create
  []
  (menu/create-menu
   :party
   (ui/create-window 0 0 constants/screen-width constants/screen-height)
   [(ui/create-label "Items" (- constants/screen-width from-right) (- constants/screen-height 20))
    (ui/create-label "Magic" (- constants/screen-width from-right) (- constants/screen-height 40))]))
