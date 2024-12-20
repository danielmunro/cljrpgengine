(ns cljrpgengine.event
  (:require [cljrpgengine.util :as util])
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [clojure.java.io :as io]))

(def events (atom []))

(def engagement (atom nil))

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
  ([conditions mob dialog outcomes]
   (swap! events conj {:type :dialog
                       :conditions (conj conditions (speaking-to mob))
                       :mob mob
                       :dialog dialog
                       :outcomes outcomes}))
  ([conditions mob dialog]
   (create-dialog-event! conditions mob dialog [])))

(defn conditions-met?
  ([conditions compare]
   (every? #(cond
              (= (:type %) :speak-to)
              (= (:mob %) compare)
              (= (:type %) :has-grant)
              (contains? (:grants @player/player) (:grant %))
              (= (:type %) :not-has-grant)
              (not (contains? (:grants @player/player) (:grant %)))
              (= (:type %) :has-item)
              (contains? (:items @player/player) (:item %))
              (= (:type %) :not-has-item)
              (not (contains? (:items @player/player) (:item %)))
              (= (:type %) :room-loaded)
              (= (:room %) compare))
           conditions))
  ([conditions]
   (conditions-met? conditions nil)))

(defn apply-outcomes!
  [outcomes]
  (dorun
   (for [outcome outcomes]
     (cond
       (= :grant (:type outcome))
       (player/add-grant! (:grant outcome))
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
  [room]
  (filter #(and
            (= (:type %) :room-loaded)
            (conditions-met? (:conditions %) room)) @events))

(defn get-dialog-event
  [target-mob]
  (util/filter-first
   #(and
     (= (:type %) :dialog)
     (conditions-met? (:conditions %) target-mob))
   @events))

(defn reset-events!
  []
  (swap! events (fn [_] [])))

(defn fire-room-loaded-event
  [room]
  (doseq [event (get-room-loaded-events (keyword room))]
    (apply-outcomes! (:outcomes event))))

(defn load-room-events!
  [scene room]
  (let [file-path (str constants/scenes-dir (name scene) "/" (name room) "/events")
        dir (io/file file-path)]
    (reset-events!)
    (if (.exists dir)
      (let [event-files (.listFiles dir)]
        (doseq [event-file event-files]
          (let [events-data (read-string (slurp (str file-path "/" (.getName event-file))))]
            (doseq [event events-data]
              (swap! events conj event))))))))

(defn create-engagement!
  [mob]
  (let [identifier (:identifier mob)
        event (get-dialog-event identifier)]
    (swap! engagement (constantly {:dialog (:dialog event)
                                   :dialog-index 0
                                   :message-index 0
                                   :mob identifier
                                   :event event
                                   :mob-direction (get-in mob [:sprite :current-animation])}))
    (swap! mob/mobs assoc-in [identifier :sprite :current-animation]
           (util/opposite-direction (:direction (player/party-leader))))))

(defn engagement-done?
  []
  (= (count (:dialog @engagement)) (:dialog-index @engagement)))

(defn clear-engagement!
  []
  (let [{:keys [mob mob-direction] {:keys [outcomes]} :event} @engagement
        current-animation-path [mob :sprite :current-animation]]
    (let [current-animation (get-in @mob/mobs current-animation-path)]
      (apply-outcomes! outcomes)
      (if (= current-animation (get-in @mob/mobs current-animation-path))
        (swap! mob/mobs assoc-in current-animation-path mob-direction))
      (swap! engagement (constantly nil)))))

(defn inc-engagement!
  []
  (swap! engagement update-in [:message-index] inc)
  (let [dialog-index (:dialog-index @engagement)]
    (if (= (count (get-in @engagement [:dialog dialog-index :messages]))
           (:message-index @engagement))
      (do
        (swap! engagement assoc :message-index 0)
        (swap! engagement update :dialog-index inc)
        (if (engagement-done?)
          (clear-engagement!))))))
