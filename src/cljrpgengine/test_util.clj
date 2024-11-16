(ns cljrpgengine.test-util
  (:require [cljrpgengine.initialize-game :as new-game]
            [cljrpgengine.player :as player]))

(defn create-new-state
  []
  (let [state (ref {})]
    (player/create-new-player)
    (new-game/start state)
    state))
