(ns cljrpgengine.event
  (:require [cljrpgengine.util :as util])
  (:require [cljrpgengine.item :as item]
            [cljrpgengine.mob :as mob]))

(defn speaking-to
  [mob]
  {:type :speak-to
   :mob mob})

(defn granted
  [grant]
  {:type :has-grant
   :grant grant})

(defn not-granted
  [grant]
  {:type :not-has-grant
   :grant grant})

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

(defn move-mob
  [mob coords]
  {:type :move-mob
   :mob mob
   :coords coords})

(defn set-mob-coords
  [mob coords]
  {:type   :set-mob-coords
   :mob    mob
   :coords coords})

(defn mob-animation
  [mob animation]
  {:type :mob-animation
   :mob mob
   :animation animation})

(defn player-animation
  [animation]
  {:type :player-animation
   :animation animation})

(defn room-loaded
  [room]
  {:type :room-loaded
   :room room})

(defn create-dialog-event!
  ([state conditions mob dialog outcomes]
   (dosync (alter state update-in [:events] conj {:type :dialog
                                                  :conditions (conj conditions (speaking-to mob))
                                                  :mob mob
                                                  :dialog dialog
                                                  :outcomes outcomes})))
  ([state conditions mob dialog]
   (create-dialog-event! state conditions mob dialog [])))

(defn create-room-loaded-event!
  [state conditions room outcomes]
  (dosync (alter state update-in [:events] conj {:type :room-loaded
                                                 :conditions (conj conditions (room-loaded room))
                                                 :room room
                                                 :outcomes outcomes})))

(defn conditions-met
  [state conditions compare]
  (every? #(cond
             (= (:type %) :speak-to)
             (= (:mob %) compare)
             (= (:type %) :has-grant)
             (contains? (:grants @state) (:grant %))
             (= (:type %) :not-has-grant)
             (not (contains? (:grants @state) (:grant %)))
             (= (:type %) :has-item)
             (contains? (:items @state) (:item %))
             (= (:type %) :not-has-item)
             (not (contains? (:items @state) (:item %)))
             (= (:type %) :room-loaded)
             (= (:room %) compare))
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
       (item/add-item! state (:item outcome))
       (= :move-mob (:type outcome))
       (mob/set-destination state (:mob outcome) (:coords outcome))
       (= :mob-animation (:type outcome))
       (mob/play-animation! state [:mobs (:mob outcome)] (:animation outcome))
       (= :player-animation (:type outcome))
       (mob/play-animation! state [:player :party 0] (:animation outcome))
       (= :set-mob-coords (:type outcome))
       (mob/set-position! state [:mobs (:mob outcome)] (:coords outcome))))))

(defn get-room-loaded-events
  [state room]
  (filter #(and
            (= (:type %) :room-loaded)
            (conditions-met state (:conditions %) room)) (:events @state)))

(defn get-dialog-event
  [state target-mob]
  (util/filter-first
   #(and
     (= (:type %) :dialog)
     (conditions-met state (:conditions %) target-mob))
   (:events @state)))

(defn fire-room-loaded-event
  [state room]
  (dorun
   (for [event (get-room-loaded-events state (keyword room))]
     (apply-outcomes! state (:outcomes event)))))
