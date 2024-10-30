(ns cljrpgengine.state
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.log :as log]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.map :as map]
            [cljrpgengine.sprite :as sprite]
            [clojure.java.io :as io]
            [java-time.api :as jt]))

(def initial-state {:save-name nil
                    :keys #{}
                    :mobs {}
                    :items {}
                    :events []
                    :menus []
                    :grants #{}
                    :scene :main-menu
                    :room nil
                    :nodes #{}
                    :player {}
                    :money 0
                    :map nil
                    :effects {}
                    :shops {}
                    :is-moving? false})

(defn- transform-to-save
  [state]
  (let [{:keys [save-name scene room grants items money]
         {:keys [party x y direction]} :player} @state]
    {:save-name save-name
     :scene scene
     :room room
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
              :direction direction}}))

(defn save
  [state]
  (let [file-name (str constants/save-dir (:save-name @state) "/" (jt/local-date-time) ".txt")]
    (log/info (format "saving progress to file :: %s" file-name))
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
   (sprite/create (:identifier data))
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
  (log/info (str "loading save file :: " constants/save-dir save-file))
  (let [data (read-string (slurp (str constants/save-dir save-file)))
        {:keys [scene room save-name grants items money]} data]
    (ref
     (merge
      initial-state
      {:scene scene
       :room room
       :save-name save-name
       :grants grants
       :items items
       :money money
       :player (load-player data)
       :map (map/load-map scene room)}))))

(defn create-new-state
  []
  (ref initial-state))
