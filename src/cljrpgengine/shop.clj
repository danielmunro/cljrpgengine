(ns cljrpgengine.shop
  (:require [cljrpgengine.constants :as constants]
            [clojure.java.io :as io]))

(def shops (atom nil))

(defn load-shops!
  [scene-name room-name]
  (let [file-path (str constants/scenes-dir (name scene-name) "/" (name room-name) "/shops")
        dir (io/file file-path)]
    (swap! shops (constantly (atom nil)))
    (if (.exists dir)
      (let [shop-files (.listFiles dir)]
        (dosync
         (doseq [shop-file shop-files]
           (let [shop-data (read-string (slurp (str file-path "/" (.getName shop-file))))]
             (swap! shops (constantly shop-data)))))))))
