(ns cljrpgengine.test-util
  (:require [cljrpgengine.initialize-game :as new-game]
            [cljrpgengine.state :as state]))

(defn create-new-state
  []
  (let [state (state/create-new-state)]
    (new-game/start state)
    state))
