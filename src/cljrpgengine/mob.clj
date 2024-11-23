(ns cljrpgengine.mob
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [clojure.java.io :as io])
  (:import (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Texture)
           (com.badlogic.gdx.graphics.g2d Animation Animation$PlayMode TextureRegion)
           (com.badlogic.gdx.scenes.scene2d Actor)
           (com.badlogic.gdx.utils Array)))

(defn sprite-array
  [txr coll]
  (Array/with (object-array (map #(-> txr (get (first %)) (get (second %))) coll))))

(defn create-mob
  [sprite-file-name]
  (let [tx (Texture. (FileHandle. (io/file (str constants/sprites-dir sprite-file-name))))
        txr (TextureRegion/split tx
                                 constants/mob-width
                                 constants/mob-height)
        down (Animation. constants/walk-speed
                         (sprite-array txr [[0 0] [0 1] [0 0] [0 2]])
                         Animation$PlayMode/LOOP)]
    (proxy [Actor] []
      (draw [batch _]
        (let [frame (.getKeyFrame down @deps/state-time true)]
          (.draw batch
                 frame
                 (float (- (/ constants/screen-width 2) (/ constants/mob-width 2)))
                 (float (- (/ constants/screen-height 2) (/ constants/mob-height 2)))))))))
