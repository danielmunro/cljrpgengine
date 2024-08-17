(ns cljrpgengine.state
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.map :as map]
            [cljrpgengine.save-file :as save-file]
            [cljrpgengine.util :as util]
            [clojure.java.io :as io]
            [clojure.string :as string]))

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

(defn create-from-latest-save
  [save-name]
  (let [directory (io/file (str "resources/saves/" save-name))
        files (sort (filter #(string/includes? % ".txt") (file-seq directory)))]
    (save-file/load-save-file (last files))))
