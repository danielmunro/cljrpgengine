(ns cljrpgengine.menu.quit
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(defn create
  []
  (let [window (ui/create-window
                (* constants/screen-height 1/10)
                (* constants/screen-width 1/10)
                (* constants/screen-width 4/5)
                (* constants/screen-height 4/5))
        label (ui/create-label "Are you sure you want to quit?"
                               0
                               (ui/line-number window 1))]
    (ui/center-in-window window label)
    (.addActor window
               label)
    (menu/create-menu
     :quit
     window
     (map #(assoc % :label (ui/center-in-window window (:label %)))
          [(menu/create-option
            (ui/create-label "Yes" 0 (ui/line-number window 3))
            #(System/exit 0))
           (menu/create-option
            (ui/create-label "No" 0 (ui/line-number window 4))
            #(menu/remove-menu!))]))))
