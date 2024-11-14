(ns cljrpgengine.state
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.log :as log]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.tilemap :as map]
            [cljrpgengine.player :as player]
            [cljrpgengine.sprite :as sprite]
            [clojure.java.io :as io]
            [java-time.api :as jt]))

(def initial-state {:save-name nil
                    :scene :main-menu
                    :room nil
                    :nodes #{}
                    :money 0
                    :effects {}
                    :shops {}})

(defn- transform-to-save
  [state]
  (let [{:keys [save-name scene room money]} @state
        {:keys [grants items]} @player/player]
    {:save-name save-name
     :scene scene
     :room room
     :money money
     :grants grants
     :items items
     :player {:party (into {} (map
                               (fn [k]
                                 (let [{:keys [name
                                               identifier
                                               class
                                               level
                                               hp
                                               max-hp
                                               mana
                                               max-mana
                                               portrait
                                               skills
                                               xp
                                               x
                                               y]} (get @player/party k)]
                                   {k {:name       name
                                       :identifier identifier
                                       :class      class
                                       :level      level
                                       :hp         hp
                                       :max-hp     max-hp
                                       :mana       mana
                                       :max-mana   max-mana
                                       :portrait   (:filename portrait)
                                       :skills     skills
                                       :x          x
                                       :y          y
                                       :xp         xp}})) (keys @player/party)))}}))

(defn save
  [state]
  (let [file-name (str constants/save-dir (:save-name @state) "/" (jt/local-date-time) ".txt")]
    (log/info (format "saving progress to file :: %s" file-name))
    (io/make-parents file-name)
    (spit file-name (transform-to-save state))
    (spit (str constants/save-dir "last-save.txt") (transform-to-save state))))

(defn mob-from-data
  [data]
  (let [mob (second data)]
    {(first data) (mob/create-mob
                   (:identifier mob)
                   (:name mob)
                   (:class mob)
                   (:level mob)
                   :down
                   (:x mob)
                   (:y mob)
                   (sprite/create (:identifier mob))
                   (:portrait mob)
                   (:skills mob)
                   (:xp mob))}))

(defn load-player
  [data]
  (let [{{:keys [party]} :player} data]
    (swap! player/player
           (fn [_] {:items (:items data)
                    :grants (:grants data)}))
    (swap! player/party
           (fn [_] (into {} (map mob-from-data party))))))

(defn load-save-file
  [save-file]
  (log/info (str "loading save file :: " constants/save-dir save-file))
  (let [data (read-string (slurp (str constants/save-dir save-file)))
        {:keys [scene room save-name money]} data]
    (load-player data)
    (map/load-tilemap scene room)
    (ref
     (merge
      initial-state
      {:scene scene
       :room room
       :save-name save-name
       :money money}))))

(defn create-new-state
  []
  (ref initial-state))
