(ns cljrpgengine.sprite
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.util :as util])
  (:import (java.awt.geom AffineTransform)
           (java.awt.image BufferedImage)))

(defn- add-default-props
  [animations]
  (into {}
        (map
         (fn [animation] {animation (assoc (animation animations) :frame 0 :is-playing false)})
         (keys animations))))

(defn create
  [name filename width height columns rows current-animation animations]
  {:name name
   :filename filename
   :image (util/load-image (str "sprites/" filename))
   :width width
   :height height
   :columns columns
   :rows rows
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
    (if (:is-playing animation)
      (get-next-frame frame (count (:frames animation)))
      frame)))

(defn draw
  [g player-x player-y {:keys [width height columns rows image animations current-animation]}]
  (let [animation (current-animation animations)
        index (:frame animation)
        frame (get (:frames animation) index)
        x (* width (mod frame columns))
        y (* height (Math/floor (/ frame rows)))
        bi (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        sprite-frame (.createGraphics bi)
        y-diff (- height width)]
    (.drawImage sprite-frame
                image
                0 0
                width height
                x
                y
                (+ x width)
                (+ y height)
                nil)
    (let [transform (AffineTransform.)]
      (.translate transform player-x (- player-y y-diff))
      (.drawImage g
                  bi
                  transform
                  nil))))

(defn create-animation
  [frames delay]
  {:frames frames
   :delay delay})

(defn create-edwyn
  [direction]
  (create
   name
   "edwyn.png"
   (first constants/character-dimensions)
   (second constants/character-dimensions)
   4
   4
   direction
   {:down  (create-animation [0 1 0 3] 8)
    :left  (create-animation [4 5 4 7] 8)
    :right (create-animation [8 9 8 11] 8)
    :up    (create-animation [12 13 12 15] 8)}))

(defn create-from-name
  [name direction]
  (cond
    (= :fireas name)
    (create-edwyn direction)
    (= :edwyn name)
    (create-edwyn direction)))
