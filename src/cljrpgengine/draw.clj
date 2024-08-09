(ns cljrpgengine.draw
  (:require [cljrpgengine.sprite :as sprite]))

(defn draw [state]
  (sprite/draw (:sprite @state) (get-in @state [:sprite :current-animation])))
