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
                  (.dispose @deps/font))
        mob (mob/create-mob "edwyn.png")
        state-time (atom (float 0))]
    (proxy [Screen] []
      (show []
        #_(reset! stage (Stage.)))
      (render [delta]
        (ScreenUtils/clear Color/BLACK)
        (.apply deps/viewport)
        (swap! state-time (fn [t] (+ t delta)))
        (let [frame (.getKeyFrame (-> mob :animations :down) @state-time true)]
          (.begin @deps/batch)
          (.draw @deps/batch frame (float (- (/ constants/screen-width 2) 8)) (float (- (/ constants/screen-height 2) 12)))
          (.end @deps/batch))
        #_(doto @stage
          (.act delta)
          (.draw)))
      (dispose []
        (dispose))
      (hide [])
      (pause [])
      (resize [w h]
        (.update deps/viewport w h true))

      (resume []))))
