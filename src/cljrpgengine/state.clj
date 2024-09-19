(ns cljrpgengine.state
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.player :as player]
            [cljrpgengine.map :as map]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [java-time.api :as jt]))

(def initial-state {:save-name nil
                    :keys      #{}
                    :mobs      []
                    :items     {:light-health-potion 2
                                :light-mana-potion 1
                                :practice-sword 1}
                    :events    []
                    :menus     []
                    :grants    #{}
                    :scene     :tinytown
                    :player    nil
                    :money     constants/starting-money
                    :map       nil})

(defn- transform-to-save
  [state]
  (let [party (get-in @state [:player :party])]
    {:save-name (:save-name @state)
     :scene     (.scene-name (:scene @state))
     :grants    (:grants @state)
     :items     (:items @state)
     :money     (:money @state)
     :player    {:party (into [] (map (fn [p] {:name   (:name p)
                                               :x      (:x p)
                                               :y (:y p)
                                               :direction (:direction p)
                                               :sprite (get-in p [:sprite :name])}) party))}
     :map {:name (name (get-in @state [:map :name]))
           :room (name (get-in @state [:map :room]))}}))

(defn save
  [state]
  (let [file-name (str "resources/saves/" (:save-name @state) "/" (jt/local-date-time) ".txt")]
    (println "saving to: " file-name)
    (io/make-parents file-name)
    (spit file-name (transform-to-save state))))

(defn load-save-file
  [save-file]
  (let [data (read-string (slurp save-file))]
    (ref
     (merge
      initial-state
      {:scene (:scene data)
       :save-name (:save-name data)
       :grants (:grants data)
       :items (:items data)
       :money (:money data)
       :player (player/load-player data)
       :map (map/load-render-map (get-in data [:map :name]) (get-in data [:map :room]))}))))

(defn create-new-state
  [player map]
  (let [state (ref
               (merge
                initial-state
                {:player player
                 :save-name (random-uuid)}))]
    (dosync
     (let [start (map/get-warp map "start")]
       (alter state assoc-in [:map] map)
       (alter state update-in [:player :party 0] assoc
              :x (:x start)
              :y (:y start)
              :direction (:direction start))))
    state))

(defn create-from-latest-save
  [save-name]
  (let [directory (io/file (str "resources/saves/" save-name))
        files (sort (filter #(string/includes? % ".txt") (file-seq directory)))]
    (load-save-file (last files))))
