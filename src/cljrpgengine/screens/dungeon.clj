(ns cljrpgengine.screens.dungeon
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.mob :as mob]
            [clojure.java.io :as io])
  (:import (com.badlogic.gdx Screen)
           (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Color Texture)
           (com.badlogic.gdx.graphics.g2d Animation Animation$PlayMode TextureRegion)
           (com.badlogic.gdx.scenes.scene2d Stage)
           (com.badlogic.gdx.utils Array ScreenUtils)))

(defn screen
  [game]
  (let [stage (atom nil)
        dispose (fn []
                  (.dispose @deps/batch)
                  (.dispose @deps/font))]
    (proxy [Screen] []
      (show []
        (reset! stage (Stage.))
        (.addActor @stage (mob/create-mob "edwyn.png")))
      (render [delta]
        (ScreenUtils/clear Color/BLACK)
        (.apply deps/viewport)
        (swap! deps/state-time (fn [t] (+ t delta)))
        #_(let [frame (.getKeyFrame (-> mob :animations :down) @state-time true)]
          (.begin @deps/batch)
          (.draw @deps/batch frame (float (- (/ constants/screen-width 2) 8)) (float (- (/ constants/screen-height 2) 12)))
          (.end @deps/batch))
        (doto @stage
          (.act delta)
          (.draw)))
      (dispose []
        (dispose))
      (hide [])
      (pause [])
      (resize [w h]
        (.update deps/viewport w h true))

      (resume []))))
