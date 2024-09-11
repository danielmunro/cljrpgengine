(ns cljrpgengine.event
  (:require [cljrpgengine.util :as util])
  (:require [cljrpgengine.item :as item]))

(defn speaking-to
  [mob]
  {:type :speak-to
   :mob mob})

(defn granted
  [grant]
  {:type :has-grant
   :grant grant})

;(defn not-granted-condition
;  [grant]
;  {:type :not-has-grant
;   :grant grant})

(defn has-item
  [item]
  {:type :has-item
   :item item})

(defn not-has-item
  [item]
  {:type :not-has-item
   :item item})

(defn grant
  [grant]
  {:type :grant
   :grant grant})

(defn lose-item
  [item]
  {:type :lose-item
   :item item})

(defn gain-item
  [item]
  {:type :gain-item
   :item item})

(defn create-dialog-event!
  ([state conditions mob dialog outcomes]
   (dosync (alter state update-in [:events] conj {:type :dialog
                                                  :conditions (conj conditions (speaking-to mob))
                                                  :mob mob
                                                  :dialog dialog
                                                  :outcomes outcomes})))
  ([state conditions mob dialog]
   (create-dialog-event! state conditions mob dialog [])))

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
       (= :lose-item (:type outcome))
       (item/remove-item! state (:item outcome))
       (= :gain-item (:type outcome))
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
