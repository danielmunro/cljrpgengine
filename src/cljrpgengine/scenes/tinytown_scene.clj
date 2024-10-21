(ns cljrpgengine.scenes.tinytown-scene
  (:require [cljrpgengine.scene :as scene]
            [cljrpgengine.state :as state]))

(def mobs (atom {}))

(defn initialize-scene
  [state]
  (state/update-nodes state #{:player :mobs :map}))

(defn update-scene
  [_])

(deftype TinytownScene [state]
  scene/Scene
  (initialize-scene [_] (initialize-scene state))
  (update-scene [_] (update-scene state))
  (scene-name [_] :tinytown)
  (shops [_] {:tinytown-item-shop [:light-health-potion
                                   :practice-sword
                                   :cotton-tunic]}))

(defn create
  [state]
  (TinytownScene. state))
