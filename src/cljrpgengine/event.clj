(ns cljrpgengine.event
  (:require [cljrpgengine.util :as util]))

(defn create-dialog-event
  [state condition mob dialog]
  (dosync (alter state update-in [:potential-events] conj {:type :dialog
                                                           :condition condition
                                                           :mob mob
                                                           :dialog dialog})))

(defn get-dialog-event
  [state mob]
  (util/filter-first
   #(and
     (= (:type %) :dialog)
     (= (:condition %) :default)
     (= (:mob %) mob))
   (:potential-events @state)))
