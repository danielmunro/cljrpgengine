(ns cljrpgengine.scenes.tinytown-scene
  (:require [cljrpgengine.map :as map]
            [cljrpgengine.scene :as scene]))

(defn initialize
  [state]
  (dosync
    (let [map (map/load-map "tinytown" "main")
          start (map/get-warp map "start")]
      (alter state update-in [:map] (constantly map))
      (alter state update-in [:player :party 0 :x] (constantly (:x start)))
      (alter state update-in [:player :party 0 :y] (constantly (:y start))))))

(deftype TinytownScene []
  scene/Scene
  (initialize [state] (initialize state)))
