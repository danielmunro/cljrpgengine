(ns cljrpgengine.save-file
  (:require [java-time.api :as jt]
            [clojure.java.io :as io]))

(defn save
  [state]
  (let [file-name (str "resources/saves/" (:save-name @state) "/" (jt/local-date-time) ".txt")]
    (println "saving to: " file-name)
    (io/make-parents file-name)
    (spit file-name @state)))
