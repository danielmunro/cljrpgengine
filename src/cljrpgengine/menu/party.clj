(ns cljrpgengine.menu.party
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.items :as item-menu]
            [cljrpgengine.ui :as ui]))

(def from-right 140)
(def x (- constants/screen-width from-right))

(defn create
  []
  (let [window (ui/create-window 0 0 constants/screen-width constants/screen-height)]
    (menu/create-menu
     :party
     window
     [(menu/create-option
       (ui/create-label "Items" x (ui/line-number window 1))
       #(menu/add-menu! (item-menu/create)))

      (menu/create-option
       (ui/create-label "Magic" x (ui/line-number window 2))
       #(println "magic menu not implemented"))

      (menu/create-option
       (ui/create-label "Equipment" x (ui/line-number window 3))
       #(println "equipment menu not implemented"))

      (menu/create-option
       (ui/create-label "Storylines" x (ui/line-number window 4))
       #(println "storylines menu not implemented"))

      (menu/create-option
       (ui/create-label "Save" x (ui/line-number window 5))
       #(println "save menu not implemented"))

      (menu/create-option
       (ui/create-label "Quit" x (ui/line-number window 6))
       #(println "quit menu not implemented"))])))
