(ns cljrpgengine.sprite
  (:require [cljrpgengine.constants :as constants]
            [quil.core :as q]))

(def create-graphics (memoize (fn [w h] (q/create-graphics w h))))

(defn- add-default-props
  [animations]
  (into {}
        (map
         (fn [animation] {animation (assoc (animation animations) :frame 0 :is-playing false)})
         (keys animations))))

(defn create
  [name filename width height current-animation animations]
  {:name name
   :filename filename
   :image (q/load-image (str "sprites/" filename))
   :width width
   :height height
   :current-animation current-animation
   :animations (add-default-props animations)})

(defn get-next-frame
  [current-frame total-frames]
  (let [next-frame (inc current-frame)]
    (if (< next-frame total-frames)
      next-frame
      0)))

(defn get-sprite-frame
  [{:keys [current-animation animations]} frame]
  (let [animation (current-animation animations)]
    (if (and
         (= 0 (mod (q/frame-count) (:delay animation)))
         (:is-playing animation))
      (get-next-frame frame (:frames animation))
      frame)))

(defn draw
  [player-x player-y {:keys [width height image animations current-animation]}]
  (let [animation (current-animation animations)
        frame (:frame animation 0)
        x (* frame width)
        y (* height (:y-offset animation))
        g (create-graphics width height)
        y-diff (- height width)]
    (q/with-graphics g
      (.clear g)
      (q/image image (- x) (- y)))
    (q/image g player-x (- player-y y-diff) width height)))

(defn create-from-name
  [name direction]
  (cond
    (= :fireas name)
    (create
     name
     "fireas.png"
     (first constants/character-dimensions)
     (second constants/character-dimensions)
     direction
     {:down  {:frames   4
              :delay    8
              :y-offset 0}
      :left  {:frames   4
              :delay    8
              :y-offset 1}
      :right {:frames   4
              :delay    8
              :y-offset 2}
      :up    {:frames   4
              :delay    8
              :y-offset 3}
      :sleep {:frames   1
              :delay    0
              :y-offset 4}})))
