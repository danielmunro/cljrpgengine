(ns cljrpgengine.scene)

(defprotocol Scene
  (initialize-scene [scene state])
  (update-scene [scene state]))
