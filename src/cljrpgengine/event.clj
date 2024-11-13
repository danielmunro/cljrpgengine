(ns cljrpgengine.event
  (:require [cljrpgengine.util :as util])
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.log :as log]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [clojure.java.io :as io]))

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
  [state conditions compare]
  (every? #(cond
             (= (:type %) :speak-to)
             (= (:mob %) compare)
             (= (:type %) :has-grant)
             (contains? (:grants @state) (:grant %))
             (= (:type %) :not-has-grant)
             (not (contains? (:grants @state) (:grant %)))
             (= (:type %) :has-item)
             (contains? (:items @player/player) (:item %))
             (= (:type %) :not-has-item)
             (not (contains? (:items @player/player) (:item %)))
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
       (player/remove-item! (:item outcome))
       (= :gain-item (:type outcome))
       (player/add-item! (:item outcome))
       (= :move-mob (:type outcome))
       (mob/set-destination! (:mob outcome) (:coords outcome))
       (= :mob-animation (:type outcome))
       (mob/play-animation! mob/mobs (:mob outcome) (:animation outcome))
       (= :player-animation (:type outcome))
       (mob/play-animation! player/party (:identifier (player/party-leader)) (:animation outcome))
       (= :set-mob-coords (:type outcome))
       (mob/set-position! (:mob outcome) (:coords outcome))))))

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
  (doseq [event (get-room-loaded-events state (keyword room))]
    (apply-outcomes! state (:outcomes event))))

(defn load-room-events
  [state scene room]
  (let [file-path (str constants/scenes-dir (name scene) "/" (name room) "/events")
        dir (io/file file-path)]
    (if (.exists dir)
      (let [event-files (.listFiles dir)]
        (doseq [event-file event-files]
          (let [events-data (read-string (slurp (str file-path "/" (.getName event-file))))]
            (doseq [event events-data]
              (dosync (alter state update-in [:events] conj event))))))
      (dosync (alter state assoc :events [])))))

(defn create-engagement!
  [state mob]
  (let [identifier (:identifier mob)
        event (get-dialog-event state identifier)]
    (dosync (alter state assoc
                   :engagement {:dialog (:dialog event)
                                :dialog-index 0
                                :message-index 0
                                :mob identifier
                                :event event
                                :mob-direction (get-in mob [:sprite :current-animation])}))
    (swap! mob/mobs assoc-in [identifier :sprite :current-animation]
           (util/opposite-direction (:direction (player/party-leader))))))

(defn engagement-done?
  [engagement]
  (= (count (:dialog engagement)) (:dialog-index engagement)))

(defn clear-engagement!
  [state]
  (let [{:keys [engagement]} @state
        {:keys [mob mob-direction] {:keys [outcomes]} :event} engagement
        current-animation-path [mob :sprite :current-animation]]
    (let [current-animation (get-in @mob/mobs current-animation-path)]
      (apply-outcomes! state outcomes)
      (if (= current-animation (get-in @mob/mobs current-animation-path))
        (swap! mob/mobs assoc-in [mob :sprite :current-animation] mob-direction))
      (dosync (alter state dissoc :engagement)))))

(defn inc-engagement!
  [state]
  (dosync (alter state update-in [:engagement :message-index] inc))
  (let [dialog-index (get-in @state [:engagement :dialog-index])]
    (if (= (count (get-in @state [:engagement :dialog dialog-index :messages]))
           (get-in @state [:engagement :message-index]))
      (do
        (dosync
         (alter state assoc-in [:engagement :message-index] 0)
         (alter state update-in [:engagement :dialog-index] inc))
        (if (engagement-done? (:engagement @state))
          (clear-engagement! state))))))
