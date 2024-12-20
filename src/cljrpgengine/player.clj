(ns cljrpgengine.player
  (:require [cljrpgengine.mob :as mob]))

(def party (atom {}))
(def items (atom {}))
(def grants (atom #{}))
(def gold (atom 100))

(defn party-leader
  []
  (first (vals @party)))

(defn add-item!
  [item]
  (if-let [quantity (get @items item)]
    (swap! items assoc item (inc quantity))
    (swap! items assoc item 1)))

(defn remove-item!
  ([item-key quantity]
   (swap! items update-in [item-key] (fn [amount] (- amount quantity)))
   (if (= 0 (get @items item-key))
     (swap! items dissoc item-key)))
  ([item-key]
   (remove-item! item-key 1)))

(defn add-grant!
  [grant]
  (swap! grants conj grant))

(defn has-grant?
  [grant]
  (contains? @grants grant))

(defn create-new-player
  []
  {:edwin (mob/create-mob
           :edwyn
           "Edwyn"
           :down
           0
           0
           :edwyn
           :warrior)
   ;:dudelgor (mob/create-mob
   ;           :dudelgor
   ;           "Dudelgor"
   ;           :down
   ;           0
   ;           0
   ;           :cyrus
   ;           :cleric)
   })
