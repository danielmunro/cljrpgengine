(ns cljrpgengine.event
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.log :as log]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [clojure.java.io :as io])
  (:import (com.badlogic.gdx.utils Align)))

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
              (player/has-grant? (:grant %))
              (= (:type %) :not-has-grant)
              (not (contains? @player/grants (:grant %)))
              (= (:type %) :has-item)
              (contains? @player/items (:item %))
              (= (:type %) :not-has-item)
              (not (contains? @player/items (:item %)))
              (= (:type %) :room-loaded)
              (= (:room %) compare))
           conditions))
  ([conditions]
   (conditions-met? conditions nil)))

(defn- move-mob
  [mob coordinates]
  (.setX (:window mob) (first coordinates))
  (.setY (:window mob) (second coordinates)))

(defn apply-outcomes!
  [outcomes]
  (doseq [outcome outcomes]
    (cond
      (= :grant (:type outcome))
      (player/add-grant! (:grant outcome))
      (= :lose-item (:type outcome))
      (player/remove-item! (:item outcome))
      (= :gain-item (:type outcome))
      (player/add-item! (:item outcome))
      (= :move-mob (:type outcome))
      ((:set-destination (get @mob/mobs (:mob outcome))) (:coords outcome))
      (= :mob-animation (:type outcome))
      ((:play-animation! ((:mob outcome) @mob/mobs)) (:animation outcome))
      (= :player-animation (:type outcome))
      ((:play-animation! (first (vals @player/party))) (:animation outcome))
      (= :set-mob-coords (:type outcome))
      (move-mob (get @mob/mobs (:mob outcome)) (:coords outcome))
      :else
      (log/info (str "unknown outcome :: " (:type outcome))))))

(defn get-room-loaded-events
  [room]
  (filter #(and
            (= (:type %) :room-loaded)
            (conditions-met? (:conditions %) room)) @events))

(defn get-dialog-event
  [target-mob]
  (first (filter
          #(and
            (= (:type %) :dialog)
            (conditions-met? (:conditions %) target-mob))
          @events)))

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

(defn get-dialog
  ([dialog dialog-index message-index]
   (let [monolog (get dialog dialog-index)
         mob-identifier (:mob monolog)
         mob (if (= :player mob-identifier)
               (player/party-leader)
               (get @mob/mobs mob-identifier))
         text (str (:name mob) ": " ((:messages monolog) message-index))]
     (if monolog
       text)))
  ([]
   (let [{:keys [dialog dialog-index message-index]} @engagement]
     (get-dialog dialog dialog-index message-index))))

(defn create-dialog-window
  [text]
  (let [height (* constants/screen-height 1/3)
        window (ui/create-window 0 0 constants/screen-width height)
        label (doto (ui/create-label text
                                     constants/padding
                                     0)
                (.setHeight (- height constants/padding))
                (.setWidth (- constants/screen-width (* 2 constants/padding)))
                (.setWrap true)
                (.setAlignment Align/topLeft))]
    (.addActor window label)
    {:window window
     :label label}))

(defn create-engagement!
  [mob]
  (let [identifier (:identifier mob)
        event (get-dialog-event identifier)]
    (swap! engagement (constantly (merge {:dialog (:dialog event)
                                          :dialog-index 0
                                          :message-index 0
                                          :mob identifier
                                          :event event
                                          :done? false
                                          :mob-direction @(:direction mob)} (create-dialog-window (get-dialog (:dialog event) 0 0)))))))

(defn engagement-done?
  []
  (= (count (:dialog @engagement)) (:dialog-index @engagement)))

(defn clear-engagement!
  []
  (let [{{:keys [outcomes]} :event} @engagement]
    (apply-outcomes! outcomes)
    (swap! engagement (constantly nil))))

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
          (swap! engagement assoc :done? true))))))
