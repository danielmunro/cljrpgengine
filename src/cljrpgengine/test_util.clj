(ns cljrpgengine.test-util
  (:require [cljrpgengine.initialize-game :as new-game]
            [cljrpgengine.player :as player]
            [cljrpgengine.state :as state]))

(defn create-new-state
  []
  (let [state (state/create-new-state)]
    (player/create-new-player)
    (new-game/start state)
    state))
