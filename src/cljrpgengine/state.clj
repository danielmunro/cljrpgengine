(ns cljrpgengine.state
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.map :as map]))

(defn create-state
  [start-area]
  (ref {:keys #{}
        :player (player/create-player)
        :map (map/load-map start-area)}))
