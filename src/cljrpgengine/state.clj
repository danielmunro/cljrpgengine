(ns cljrpgengine.state
  (:require [cljrpgengine.all-scenes :as all-scenes]
            [cljrpgengine.player :as player]
            [cljrpgengine.map :as map]
            [cljrpgengine.scenes.tinytown-scene :as tinytown-scene]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [java-time.api :as jt]))

(defn- transform-to-save
  [state]
  (let [party (get-in @state [:player :party])]
    {:save-name (:save-name @state)
     :scene (name (:scene @state))
     :player {:party (into [] (map (fn [p] {:name (:name p)
                                            :x (:x p)
                                            :y (:y p)
                                            :sprite (get-in p [:sprite :name])}) party))}
     :map {:name (get-in @state [:map :name])
           :room (get-in @state [:map :room])}}))

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
          :mobs #{}
          :scene (keyword (:scene data))
          :save-name (:save-name data)
          :player (player/create-new-player
                   (get-in data [:player :party 0 :x])
                   (get-in data [:player :party 0 :y]))
          :map (map/load-map (get-in data [:map :name]) (get-in data [:map :room]))})))

(defn create-new-state
  []
  (let [player (player/create-new-player 0 0)
        scene (all-scenes/scenes :tinytown)
        state (ref {:save-name (random-uuid)
                    :keys #{}
                    :mobs #{}
                    :scene :tinytown
                    :player player
                    :map nil})]
    (.initialize-scene scene state)
    state))

(defn create-from-latest-save
  [save-name]
  (let [directory (io/file (str "resources/saves/" save-name))
        files (sort (filter #(string/includes? % ".txt") (file-seq directory)))]
    (load-save-file (last files))))
