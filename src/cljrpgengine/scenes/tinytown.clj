(ns cljrpgengine.scenes.tinytown
  (:require [cljrpgengine.map :as map]))

(defn load-scene
  [state]
  (dosync
    (alter state ref-set :map (map/load-map "tinytown" "main"))))
