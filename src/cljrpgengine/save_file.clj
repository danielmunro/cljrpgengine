(ns cljrpgengine.save-file
  (:require [cljrpgengine.map :as map]
            [cljrpgengine.player :as player]
            [java-time.api :as jt]
            [clojure.java.io :as io]))

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
