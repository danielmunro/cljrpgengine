(ns cljrpgengine.deps
  (:require [cljrpgengine.constants :as constants])
  (:import (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Color OrthographicCamera)
           (com.badlogic.gdx.graphics.g2d SpriteBatch)
           (com.badlogic.gdx.graphics.g2d.freetype FreeTypeFontGenerator FreeTypeFontGenerator$FreeTypeFontParameter)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer)
           (com.badlogic.gdx.utils.viewport FitViewport)))

(def batch (atom nil))
(def font (atom nil))
(def camera (atom nil))
(def viewport (atom nil))
(def shape (atom nil))

(defn init-dependencies
  []
  (swap! batch (constantly (SpriteBatch.)))
  (swap! camera (constantly (OrthographicCamera.)))
  (swap! viewport (constantly (FitViewport. (/ constants/screen-width constants/tile-size)
                                            (/ constants/screen-height constants/tile-size)
                                            camera)))
  (swap! shape (constantly (ShapeRenderer.)))
  (let [generator (FreeTypeFontGenerator. (FileHandle. ^String constants/font-file))
        parameters (FreeTypeFontGenerator$FreeTypeFontParameter.)]
    (set! (. parameters -size) constants/font-size)
    (set! (. parameters -color) Color/WHITE)
    (swap! font (constantly (.generateFont generator parameters))))
