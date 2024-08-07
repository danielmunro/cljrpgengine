(ns cljrpgengine.draw
  (:require [cljrpgengine.sprite :as sprite]))

(defn draw [state]
  (sprite/draw (get-in @state [:player :sprite]) (get-in @state [:player :facing])))
