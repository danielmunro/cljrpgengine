(ns cljrpgengine.event
  (:require [cljrpgengine.util :as util])
  (:require [clojure.set :refer [union difference]]))

(defn speak-to-condition
  [mob]
  {:type :speak-to
   :mob mob})

(defn grants-condition
  [grants]
  {:type :has-grants
   :grants grants})

(defn create-dialog-event!
  [state conditions mob dialog grants]
  (dosync (alter state update-in [:events] conj {:type :dialog
                                                 :conditions conditions
                                                 :mob mob
                                                 :dialog dialog
                                                 :grants grants})))

(defn conditions-met
  [state conditions target-mob]
  (every? #(cond
             (= (:type %) :speak-to)
             (= (:mob %) target-mob)
             (= (:type %) :has-grants)
             (empty? (difference (:grants %) (:grants @state))))
          conditions))

(defn get-dialog-event!
  [state target-mob]
  (let [event (util/filter-first
               #(and
                 (= (:type %) :dialog)
                 (conditions-met state (:conditions %) target-mob))
               (:events @state))]
    (alter state update-in [:grants] union (:grants event))
    event))
