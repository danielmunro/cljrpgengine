(ns cljrpgengine.scene
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.log :as log]
            [clojure.java.io :as io]))

(defn load-scene
  [state scene room]
  (log/info (str "loading scene file :: " constants/scenes-dir (name scene) "/scene.edn"))
  (let [file-path (str constants/scenes-dir (name scene) "/scene.edn")
        dir (io/file file-path)]
    (if (.exists dir)
      (let [data (read-string (slurp file-path))]
        (dosync (alter state assoc
                       :scene scene
                       :room room
                       :nodes (:nodes data)))
        (if (:menu data)
          (dosync (alter state assoc :load-menu (:menu data)))))
      (throw (ex-info "scene missing scene data" {:scene scene})))))
