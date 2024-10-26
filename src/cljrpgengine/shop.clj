(ns cljrpgengine.shop
  (:require [cljrpgengine.constants :as constants]
            [clojure.java.io :as io]))

(defn load-shops
  [state area room]
  (let [file-path (str constants/scenes-dir (name area) "/" (name room) "/shops")
        dir (io/file file-path)]
    (if (.exists dir)
      (let [shop-files (.listFiles dir)]
        (dosync
         (dorun
          (for [shop-file shop-files]
            (let [shop-data (read-string (slurp (str file-path "/" (.getName shop-file))))]
              (alter state assoc-in [:shops] shop-data)))))))))
