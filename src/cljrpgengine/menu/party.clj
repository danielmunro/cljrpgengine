(ns cljrpgengine.menu.party
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(def from-right 120)
(def x (- constants/screen-width from-right))

(defn create
  []
  (let [window (ui/create-window 0 0 constants/screen-width constants/screen-height)]
    (menu/create-menu
     :party
     window
     [(ui/create-label "Items" x (ui/line-number window 1))
      (ui/create-label "Magic" x (ui/line-number window 2))
      (ui/create-label "Equipment" x (ui/line-number window 3))
      (ui/create-label "Storylines" x (ui/line-number window 4))
      (ui/create-label "Save" x (ui/line-number window 5))])))
