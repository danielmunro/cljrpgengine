(ns cljrpgengine.sprite
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.util :as util]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.awt.geom AffineTransform)
           (java.awt.image BufferedImage)))

(def move-animations
  #{:up :down :left :right})

(def sprites (atom nil))

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

(defn create-from-def
  [name]
  (let [sprite-def (or (util/filter-first #(= (:name %) name) @sprites)
                       (throw (ex-info (format "no sprite with name %s" name) {:name name})))
        {:keys [name
                file
                dimensions
                size
                default-animation
                animations]} sprite-def]
    (create
      name
      file
      (first dimensions)
      (second dimensions)
      (first size)
      (second size)
      default-animation
      animations)))

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
  (let [animation (get animations current-animation)
        {:keys [frame frames]
         {:keys [flip]} :props} animation
        index (get frames frame)
        x (* width (mod index columns))
        y (* height (Math/floor (/ index rows)))
        bi (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        sprite-frame (.createGraphics bi)
        y-diff (- height width)
        dx1 (if flip (+ x width) x)
        dx2 (if flip x (+ x width))]
    (.drawImage sprite-frame
                image
                0 0
                width height
                dx1 y
                dx2 (+ y height)
                nil)
    (let [transform (AffineTransform.)]
      (.translate transform player-x (- player-y y-diff))
      (.drawImage g
                  bi
                  transform
                  nil))))

(defn load-sprites
  []
  (swap!
   sprites
   (fn [_]
     (->> (io/file constants/sprites-dir)
          (.list)
          (seq)
          (filter #(str/ends-with? % ".edn"))
          (map
            #(read-string (slurp (str constants/sprites-dir %))))))))
