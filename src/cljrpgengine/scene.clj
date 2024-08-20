(ns cljrpgengine.scene)

(defprotocol Scene
  (initialize-scene [state])
  (update-scene [state]))
