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
