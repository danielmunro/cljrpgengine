(ns cljrpgengine.scenes.tinytown-scene
  (:require [cljrpgengine.scene :as scene]
            [cljrpgengine.state :as state]))

(deftype TinytownScene [state]
  scene/Scene
  (initialize-scene [_]
    (state/update-nodes state #{:player :mobs :map}))
  (update-scene [_])
  (scene-name [_] :tinytown))

(defn create
  [state]
  (TinytownScene. state))
