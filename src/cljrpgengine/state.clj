(ns cljrpgengine.state
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.map :as map]
            [cljrpgengine.util :as util]))

(defn create-state
  [start-area]
  (let [map (map/load-map start-area)
        start (util/filter-first #(= (:name %) "start") (get-in map [:tilemap :warps]))]
    (if (not start)
      (throw (AssertionError. "no start warp found for scene")))
    (ref {:keys #{}
          :player (player/create-new-player (:x start) (:y start))
          :map map})))
