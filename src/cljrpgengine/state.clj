(ns cljrpgengine.state
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.map :as map]
            [cljrpgengine.util :as util]))

(defn create-new-state
  [start-area]
  (let [map (map/load-map start-area)
        start (util/filter-first #(= (:name %) "start") (get-in map [:tilemap :warps]))
        player (player/create-new-player (:x start) (:y start))]
    (if (not start)
      (throw (AssertionError. "no start warp found for scene")))
    (ref {:save-name (random-uuid)
          :keys #{}
          :player player
          :map map})))
