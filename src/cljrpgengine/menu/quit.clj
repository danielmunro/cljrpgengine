(ns cljrpgengine.menu.quit
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(defn create
  []
  (let [window (ui/create-window
                 ;(* constants/screen-height 1/10)
                 ;(* constants/screen-width 1/10)
                 ;(* constants/screen-width 4/5)
                 ;(* constants/screen-height 4/5)
                 64
                 40
                 512
                 320
                 )]
    (.addActor window
               (ui/create-label "Are you sure you want to quit?"
                                10
                                (ui/line-number window 1)))
    (menu/create-menu
      :quit
      window
      [(menu/create-option
         (ui/create-label "Yes" 10 (ui/line-number window 3))
         #(System/exit 0))
       (menu/create-option
         (ui/create-label "No" 10 (ui/line-number window 4))
         #(menu/remove-menu!))])))
