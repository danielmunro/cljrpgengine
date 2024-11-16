(ns cljrpgengine.effect
  (:require [cljrpgengine.input :as input]
            [cljrpgengine.window :as window])
  (:import (java.awt Color)))

(def effects (atom []))

(defn create-effect
  [effect-type effect-fn after-fn]
  {:id (random-uuid)
   :effect-type effect-type
   :effect-fn effect-fn
   :after-fn after-fn})

(defn add-effect
  [effect]
  (swap! effects conj effect))

(defn fade-in
  [effect]
  (let [fade (- (:fade effect) 0.05)]
    (window/fill-screen (Color. (float 0) (float 0) (float 0) (float (:fade effect))))
    (assoc effect :fade fade
           :end (< fade 0))))

(defn add-fade-in
  []
  (swap! input/locked (constantly true))
  (add-effect
   (assoc (create-effect
           :fade-in
           fade-in
           #(swap! input/locked (constantly false)))
          :fade 1)))

(defn apply-effects
  []
  (let [[in-progress-effects finished-effects] (->> @effects
                                                    (map #((:effect-fn %) %))
                                                    (split-with #(false? (:end %))))]
    (doseq [e finished-effects]
      ((:after-fn e)))
    (swap! effects (constantly in-progress-effects))))
