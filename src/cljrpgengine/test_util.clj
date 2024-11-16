(ns cljrpgengine.test-util
  (:require [cljrpgengine.initialize-game :as new-game]
            [cljrpgengine.player :as player]))

(defn create-new-game
  []
  (player/create-new-player)
  (new-game/start))
