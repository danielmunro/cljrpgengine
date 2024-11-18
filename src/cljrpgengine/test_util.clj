(ns cljrpgengine.test-util
  (:require [cljrpgengine.initialize-game :as new-game]
            [cljrpgengine.player :as player]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.tilemap :as tilemap]))

(defn setup-new-player
  []
  (sprite/load-sprites)
  (player/create-new-player)
  (swap! tilemap/opened-chests (constantly #{})))

(defn create-new-game
  []
  (player/create-new-player)
  (new-game/start))
