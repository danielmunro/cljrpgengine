(ns cljrpgengine.scene)

(defprotocol Scene
  (initialize-scene [scene])
  (update-scene [scene])
  (scene-name [scene])
  (shops [scene]))
