(ns cljrpgengine.log)

(def log-levels {:debug 0
                 :info 1
                 :warn 2
                 :error 3})

(def log-level (atom :info))

(defn debug
  [message]
  (if (= 0 (get log-levels @log-level))
    (println message)))

(defn info
  [message]
  (if (< 0 (get log-levels @log-level))
    (println message)))

(defn error
  [message]
  (if (< 0 (get log-levels @log-level))
    (println message)))
