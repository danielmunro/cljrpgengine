(ns cljrpgengine.state
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.map :as map]))

(defn create-state
  [start-area]
  (let [map (map/load-map start-area)
        start (first (filter #(= (:name %) "start") (get-in map [:tilemap :warps])))]
    (if (not start)
      (throw (AssertionError. "no start warp found for scene")))
    (ref {:keys #{}
          :player (player/create-player (:x start) (:y start))
          :map map})))
