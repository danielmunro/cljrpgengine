(ns cljrpgengine.state
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.map :as map]
            [cljrpgengine.prefab-sprites :as prefab-sprites]
            [clojure.java.io :as io]
            [java-time.api :as jt]))

(def initial-state {:save-name nil
                    :keys #{}
                    :mobs []
                    :items {}
                    :events []
                    :menus []
                    :grants #{}
                    :scene :main-menu
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
     :player {:party (mapv
                      (fn [{:keys [name
                                   identifier
                                   class
                                   level
                                   hp
                                   max-hp
                                   mana
                                   max-mana
                                   portrait]}]
                        {:name name
                         :identifier identifier
                         :class class
                         :level level
                         :hp hp
                         :max-hp max-hp
                         :mana mana
                         :max-mana max-mana
                         :portrait (:filename portrait)}) party)
              :x x
              :y y
              :direction direction}
     :map {:name (name area-name)
           :room (name room)}}))

(defn save
  [state]
  (let [file-name (str constants/save-dir (:save-name @state) "/" (jt/local-date-time) ".txt")]
    (println "saving to: " file-name)
    (io/make-parents file-name)
    (spit file-name (transform-to-save state))
    (spit (str constants/save-dir "last-save.txt") (transform-to-save state))))

(defn mob-from-data
  [data]
  (mob/create-mob
    (:identifier data)
    (:name data)
    (:class data)
    (:level data)
    :down
    0
    0
    (prefab-sprites/create-from-name (:identifier data) :down)
    (:portrait data)))

(defn load-player
  [data]
  (let [{{:keys [x y direction party]} :player} data]
    {:x x
     :x-offset 0
     :y y
     :y-offset 0
     :direction direction
     :party (mapv mob-from-data party)}))

(defn load-save-file
  [save-file]
  (println (str "loading save file: " constants/save-dir save-file))
  (let [data (read-string (slurp (str constants/save-dir save-file)))
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
       :player (load-player data)
       :map (map/load-map area-name room)}))))

(defn create-new-state
  []
  (ref initial-state))

(defn update-nodes
  [state nodes]
  (dosync (alter state assoc :nodes nodes)))
