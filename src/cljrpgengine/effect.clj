(ns cljrpgengine.effect
  (:require [cljrpgengine.constants :as constants]
            [quil.core :as q]))

(defn add-effect
  [state effect-type effect effect-end]
  (dosync (alter state assoc-in [:effects effect-type]
                 {:effect-type effect-type
                  :effect effect
                  :end effect-end})))

(defn fade-in
  [state]
  (let [fade (:fade-in @state)]
    (if fade
      (dosync
        (alter state update :fade-in #(- % 10))
        (q/fill (q/color 0 0 0 fade))
        (q/rect 0 0 (first constants/window) (second constants/window))))
    (if (< (:fade-in @state) 0)
      (do
        #_((-> (:effects state)
            (:fade-in)
            (:end) state))
        true))))

(defn add-fade-in
  [state]
  (dosync (alter state assoc :fade-in 255))
  (add-effect state
              :fade-in
              fade-in
              #(dosync
                 (println "unsetting lock")
                 (alter % dissoc :lock))))

(defn apply-effects
  [state]
  (dorun
    (for [e (vals (:effects @state))]
      (let [result ((:effect e) state)]
        (if result
          (dosync (alter state update-in [:effects] dissoc (:effect-type e))))))))
