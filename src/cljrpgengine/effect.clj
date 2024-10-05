(ns cljrpgengine.effect
  (:require [cljrpgengine.window :as window])
  (:import (java.awt Color)))

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
       (alter state update :fade-in #(- % 0.05))
       (window/fill-screen (Color. (float 0) (float 0) (float 0) (float fade)))))
    (if (< (:fade-in @state) 0)
      (do
        ((-> (:effects @state)
             (:fade-in)
             (:end)) state)
        true))))

(defn add-fade-in
  [state]
  (dosync (alter state assoc :lock true
                 :fade-in 1))
  (add-effect state
              :fade-in
              fade-in
              #(dosync (alter % dissoc :lock))))

(defn apply-effects
  [state]
  (dorun
   (for [e (vals (:effects @state))]
     (let [result ((:effect e) state)]
       (if result
         (dosync (alter state update-in [:effects] dissoc (:effect-type e))))))))
