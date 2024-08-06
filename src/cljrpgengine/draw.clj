(ns cljrpgengine.draw
  (:require [cljrpgengine.sprite :as sprite]))

(defn draw [state]
  (sprite/draw @(:player state)))
