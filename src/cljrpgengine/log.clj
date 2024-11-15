(ns cljrpgengine.log)

(def log-levels {:debug 3
                 :info 2
                 :warn 1
                 :error 0})

(def log-level (atom :info))

(defn debug
  [message]
  (if (<= 3 (get log-levels @log-level))
    (println message)))

(defn info
  [message]
  (if (<= 2 (get log-levels @log-level))
    (println message)))

(defn error
  [message]
  (if (<= 0 (get log-levels @log-level))
    (println message)))
