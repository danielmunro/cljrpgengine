(ns cljrpgengine.menu.items
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(defn create
  []
  (let [window (ui/create-window 0 0 constants/screen-width constants/screen-height)]
    (menu/create-menu
     :items
     window
     [(menu/create-option
       (ui/create-label "test" 30 (ui/line-number window 1))
       #(println "foo"))])))
