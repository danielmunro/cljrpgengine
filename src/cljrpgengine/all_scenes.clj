(ns cljrpgengine.all-scenes
  (:require [cljrpgengine.scenes.tinytown-scene :as tinytown-scene]))

(def scenes {:tinytown (tinytown-scene/create-tinytown-scene)})
