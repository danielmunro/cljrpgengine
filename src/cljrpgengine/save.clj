(ns cljrpgengine.save
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.log :as log]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.scene :as scene]
            [cljrpgengine.player :as player]
            [cljrpgengine.sprite :as sprite]
            [clojure.java.io :as io]
            [java-time.api :as jt]))

(def current-save-name (atom nil))

(defn set-new-current-save-name
  []
  (swap! current-save-name (constantly (random-uuid))))

(defn- transform-to-save
  []
  (let [{:keys [grants items gold]} @player/player
        {scene-name :name room :room} @scene/scene]
    {:save-name @current-save-name
     :scene scene-name
     :room room
     :gold gold
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
  []
  (let [file-name (str constants/save-dir @current-save-name "/" (jt/local-date-time) ".txt")
        save-data (transform-to-save)]
    (log/info (format "saving progress to file :: %s" file-name))
    (io/make-parents file-name)
    (spit file-name save-data)
    (spit (str constants/save-dir "last-save.txt") save-data)))

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

(defn load-player!
  [data]
  (let [{{:keys [party]} :player} data]
    (swap! player/player
           (fn [_] {:items (:items data)
                    :grants (:grants data)
                    :gold (:gold data)}))
    (swap! player/party
           (fn [_] (into {} (map mob-from-data party))))))

(defn load-save-file
  [save-file]
  (log/info (str "loading save file :: " constants/save-dir save-file))
  (let [data (read-string (slurp (str constants/save-dir save-file)))]
    (swap! current-save-name (constantly (:save-name data)))
    data))
