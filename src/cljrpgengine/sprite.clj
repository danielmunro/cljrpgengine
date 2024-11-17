(ns cljrpgengine.sprite
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.util :as util]
            [cljrpgengine.window :as window]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.awt.geom AffineTransform)
           (java.awt.image BufferedImage)))

(def move-animations
  #{:up :down :left :right})

(def sprites (atom nil))

(defn create
  [name]
  (let [sprite-def (or (util/filter-first #(= (:name %) name) @sprites)
                       (throw (ex-info (format "no sprite with name %s" name) {:name name})))
        {:keys [file default-animation animations]} sprite-def]
    (assoc sprite-def :image (util/load-image (str constants/sprites-dir file))
           :current-animation default-animation
           :animations (into {}
                             (map
                              (fn [animation] {animation (assoc (animation animations)
                                                                :frame 0
                                                                :is-playing false
                                                                :time-elapsed 0)})
                              (keys animations))))))

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
  [x y {:keys [image animations current-animation]
        [width height] :dimensions
        [rows columns] :size}]
  (let [animation (get animations current-animation)
        {:keys [frame frames]
         {:keys [flip]} :props} animation
        i (get frames frame)
        index (if (map? i) (:frame i) i)
        to-flip (or flip (and (map? i) (:flip i)))
        col (* width (mod index columns))
        row (* height (Math/floor (/ index rows)))
        bi (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        sprite-frame (.createGraphics bi)
        y-diff (- height width)
        dx1 (if to-flip (+ col width) col)
        dx2 (if to-flip col (+ col width))]
    (.drawImage sprite-frame
                image
                0 0
                width height
                dx1 row
                dx2 (+ row height)
                nil)
    (let [transform (AffineTransform.)]
      (.translate transform x (- y y-diff))
      (.drawImage @window/graphics
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
