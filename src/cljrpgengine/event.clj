(ns cljrpgengine.event
  (:require [cljrpgengine.util :as util])
  (:require [cljrpgengine.item :as item]
            [clojure.set :refer [union difference]]))

(defn speak-to-condition
  [mob]
  {:type :speak-to
   :mob mob})

(defn granted-condition
  [grant]
  {:type :has-grant
   :grant grant})

(defn not-granted-condition
  [grant]
  {:type :not-has-grant
   :grant grant})

(defn has-item-condition
  [item]
  {:type :has-item
   :item item})

(defn not-has-item-condition
  [item]
  {:type :not-has-item
   :item item})

(defn grant-outcome
  [grant]
  {:type :grant
   :grant grant})

(defn give-item-outcome
  [item]
  {:type  :give-item
   :item item})

(defn receive-item-outcome
  [item]
  {:type :receive-item
   :item item})

(defn create-dialog-event!
  [state conditions mob dialog outcomes]
  (dosync (alter state update-in [:events] conj {:type :dialog
                                                 :conditions (conj conditions (speak-to-condition mob))
                                                 :mob mob
                                                 :dialog dialog
                                                 :outcomes outcomes})))

(defn conditions-met
  [state conditions target-mob]
  (every? #(cond
             (= (:type %) :speak-to)
             (= (:mob %) target-mob)
             (= (:type %) :has-grant)
             (contains? (:grants @state) (:grant %))
             (= (:type %) :not-has-grant)
             (not (contains? (:grants @state) (:grant %)))
             (= (:type %) :has-item)
             (seq (filter (fn [item] (= (:key item) (:item %))) (:items @state)))
             (= (:type %) :not-has-item)
             (not (seq (filter (fn [item] (= (:key item) (:item %))) (:items @state)))))
          conditions))

(defn apply-outcomes!
  [state outcomes]
  (dorun
   (for [outcome outcomes]
     (cond
       (= :grant (:type outcome))
       (dosync (alter state update-in [:grants] conj (:grant outcome)))
       (= :give-item (:type outcome))
       (item/remove-item! state (:item outcome))
       (= :receive-item (:type outcome))
       (item/add-item! state (:item outcome))))))

(defn get-dialog-event!
  [state target-mob]
  (let [event (util/filter-first
               #(and
                 (= (:type %) :dialog)
                 (conditions-met state (:conditions %) target-mob))
               (:events @state))]
    (apply-outcomes! state (:outcomes event))
    event))
