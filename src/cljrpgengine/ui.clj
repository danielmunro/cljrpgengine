(ns cljrpgengine.ui
  (:require [cljrpgengine.constants :as constants]
            [quil.core :as q]))

(defn dialog
  [message]
  (let [y (* (constants/window 1) 2/3)]
    (q/fill 0 0 255)
    (q/rect 0 y (constants/window 0) (* (constants/window 1) 1/3))
    (q/fill 255 255 255)
    (q/text message 10 (+ 20 y))))

(defn party-menu
  []
  (q/fill 0 0 255)
  (q/rect 0 0 (constants/window 0) (constants/window 1))
  (q/text "Items" (* 3/4 (constants/window 0)) 20)
  (q/text "Magic" (* 3/4 (constants/window 0)) 40)
  (q/text "Quests" (* 3/4 (constants/window 0)) 60)
  (q/text "Save" (* 3/4 (constants/window 0)) 80)
  (q/text "Exit" (* 3/4 (constants/window 0)) 100))

(defn draw-menus
  [menus]
  (dorun
    (for [m menus]
      (cond
        (= :party-menu m)
        (party-menu)))))
