(ns cljrpgengine.state
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.player :as player]
            [cljrpgengine.map :as map]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [java-time.api :as jt]))

(def initial-state {:save-name nil
                    :keys #{}
                    :mobs []
                    :items {:light-health-potion 2
                            :light-mana-potion 1
                            :practice-sword 1}
                    :events []
                    :menus []
                    :grants #{}
                    :scene :tinytown
                    :player nil
                    :money constants/starting-money
                    :map nil})

(defn- transform-to-save
  [state]
  (let [party (get-in @state [:player :party])
        {:keys [save-name scene grants items money]
         {area-name :name room :room} :map} @state]
    {:save-name save-name
     :scene (.scene-name scene)
     :grants grants
     :items items
     :money money
     :player {:party (into []
                           (map
                            (fn [{:keys [name x y direction]
                                  {sprite-name :name} :sprite}]
                              {:name name
                               :x x
                               :y y
                               :direction direction
                               :sprite sprite-name}) party))}
     :map {:name (name area-name)
           :room (name room)}}))

(defn save
  [state]
  (let [file-name (str "resources/saves/" (:save-name @state) "/" (jt/local-date-time) ".txt")]
    (println "saving to: " file-name)
    (io/make-parents file-name)
    (spit file-name (transform-to-save state))))

(defn load-save-file
  [save-file]
  (let [data (read-string (slurp save-file))
        {:keys [scene save-name grants items money]
         {area-name :name room :room} :map} data]
    (ref
     (merge
      initial-state
      {:scene scene
       :save-name save-name
       :grants grants
       :items items
       :money money
       :player (player/load-player data)
       :map (map/load-render-map area-name room)}))))

(defn create-new-state
  [player map]
  (let [state (ref
               (merge
                initial-state
                {:player player
                 :save-name (random-uuid)}))]
    (dosync
     (let [{:keys [x y direction]} (map/get-warp map "start")]
       (alter state assoc-in [:map] map)
       (alter state update-in [:player :party 0] assoc
              :x x
              :y y
              :direction direction)))
    state))

(defn create-from-latest-save
  [save-name]
  (let [directory (io/file (str "resources/saves/" save-name))
        files (sort (filter #(string/includes? % ".txt") (file-seq directory)))]
    (load-save-file (last files))))
