(ns cljrpgengine.state
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.map :as map]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [java-time.api :as jt]))

(def initial-state {:save-name nil
                    :keys #{}
                    :mobs []
                    :items {}
                    :events []
                    :menus []
                    :grants #{}
                    :scene :main-menu
                    ;:scene :tinytown
                    :nodes #{}
                    :player {}
                    :money 0
                    :map nil
                    :effects {}})

(defn- transform-to-save
  [state]
  (let [{:keys [save-name scene grants items money]
         {:keys [party x y direction]} :player
         {area-name :name room :room} :map} @state]
    {:save-name save-name
     :scene (.scene-name scene)
     :grants grants
     :items items
     :money money
     :player {:party (into []
                           (map
                            (fn [{:keys [name]
                                  {sprite-name :name} :sprite}]
                              {:name name
                               :sprite sprite-name}) party))
              :x x
              :y y
              :direction direction}
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
  []
  (ref initial-state))

(defn create-from-latest-save
  [save-name]
  (let [directory (io/file (str "resources/saves/" save-name))
        files (sort (filter #(string/includes? % ".txt") (file-seq directory)))]
    (load-save-file (last files))))

(defn update-nodes
  [state nodes]
  (dosync (alter state assoc :nodes nodes)))
