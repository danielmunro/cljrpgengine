(ns cljrpgengine.create-scene
  (:require [cljrpgengine.scenes.tinytown-scene :as tinytown-scene]
            [cljrpgengine.scenes.main-menu-scene :as main-menu-scene]))

(defn create
  [state scene]
  (cond
    (= :tinytown scene)
    (tinytown-scene/create state)
    (= :main-menu scene)
    (main-menu-scene/create state)))
