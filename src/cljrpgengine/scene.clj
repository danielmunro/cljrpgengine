(ns cljrpgengine.scene
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.log :as log]
            [clojure.java.io :as io]))

(def scene (atom {:name nil
                  :room nil
                  :nodes nil}))

(defn load-scene
  [scene-name room]
  (log/info (str "loading scene file :: " constants/scenes-dir (name scene-name) "/scene.edn"))
  (let [file-path (str constants/scenes-dir (name scene-name) "/scene.edn")
        dir (io/file file-path)]
    (if (.exists dir)
      (let [data (read-string (slurp file-path))]
        (swap! scene assoc
               :name scene-name
               :room room
               :nodes (:nodes data)))
      (throw (ex-info "scene missing scene data" {:scene scene-name})))))

(defn has-node?
  [node]
  (contains? (:nodes @scene) node))
