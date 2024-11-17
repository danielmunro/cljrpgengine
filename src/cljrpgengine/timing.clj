(ns cljrpgengine.timing
  (:require [cljrpgengine.constants :as constants]))

(def current-time (atom nil))
(def time-difference (atom nil))
(def last-time (atom (System/nanoTime)))
(def timer (atom 0))
(def draws (atom 0))
(def sleep-length (atom 12))

(defn sleep
  []
  (Thread/sleep @sleep-length))

(defn start-loop!
  []
  (swap! current-time (constantly (System/nanoTime)))
  (swap! time-difference (constantly (- current-time @last-time))))

(defn end-loop!
  []
  (swap! timer (fn [amount] (+ amount @time-difference)))
  (swap! draws inc)
  (if (< constants/nano-per-second @timer)
    (do
      (if (> @draws constants/target-fps)
        (swap! sleep-length inc)
        (swap! sleep-length dec))
      (swap! draws (fn [_] 0))
      (swap! timer (fn [amount] (- amount constants/nano-per-second)))))
  (swap! last-time (fn [_] current-time)))
