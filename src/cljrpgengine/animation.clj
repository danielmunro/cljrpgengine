(ns cljrpgengine.animation
  (:require [cljrpgengine.constants :as constants])
  (:import (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Texture)
           (com.badlogic.gdx.graphics.g2d Animation TextureRegion)
           (com.badlogic.gdx.utils Array)))

(defn- sprite-array
  [txr coll]
  (Array/with (object-array (map #(-> txr (get (first %)) (get (second %))) coll))))

(defn- create-animation
  [sprite-file-name]
  (let [tx (Texture. (FileHandle. (str constants/sprites-dir sprite-file-name)))
        txr (TextureRegion/split tx
                                 constants/mob-width
                                 constants/mob-height)]
    {:down (Animation. (float constants/walk-animation-speed)
                       ^Array (sprite-array txr [[0 0] [0 1] [0 0] [0 2]]))
     :up   (Animation. (float constants/walk-animation-speed)
                       ^Array (sprite-array txr [[3 0] [3 1] [3 0] [3 2]]))
     :left (Animation. (float constants/walk-animation-speed)
                       ^Array (sprite-array txr [[1 0] [1 1] [1 0] [1 2]]))
     :right (Animation. (float constants/walk-animation-speed)
                        ^Array (sprite-array txr [[2 0] [2 1] [2 0] [2 2]]))
     :dance (Animation. (float constants/walk-animation-speed)
                        ^Array (sprite-array txr [[1 0] [3 0] [2 0] [0 0]]))}))

(defn create-from-type
  [name]
  (case name
    :edwyn (create-animation "edwyn.png")
    :cyrus (create-animation "cyrus.png")
    false))
