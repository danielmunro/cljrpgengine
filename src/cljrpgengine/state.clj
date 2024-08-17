(ns cljrpgengine.state
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.map :as map]
            [cljrpgengine.util :as util]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [java-time.api :as jt]))

(defn- transform-to-save
  [state]
  (let [party (get-in @state [:player :party])]
    {:save-name (:save-name @state)
     :player {:party (into [] (map (fn [p] {:name (:name p)
                                            :x (:x p)
                                            :y (:y p)
                                            :sprite (get-in p [:sprite :name])}) party))}
     :map (get-in @state [:map :name])}))

(defn save
  [state]
  (let [file-name (str "resources/saves/" (:save-name @state) "/" (jt/local-date-time) ".txt")]
    (println "saving to: " file-name)
    (io/make-parents file-name)
    (spit file-name (transform-to-save state))))

(defn load-save-file
  [save-file]
  (let [data (read-string (slurp save-file))]
    (ref {:keys #{}
          :save-name (:save-name data)
          :player (player/create-new-player
                    (get-in data [:player :party 0 :x])
                    (get-in data [:player :party 0 :y]))
          :map (map/load-map (:map data))})))

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
    (load-save-file (last files))))
