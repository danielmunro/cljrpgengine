(ns cljrpgengine.create-scene
  (:require [cljrpgengine.scenes.tinytown-scene :as tinytown-scene]))

(defn create
  [state scene]
  (cond
    (= :tinytown scene)
    (tinytown-scene/create-tinytown-scene state)))
