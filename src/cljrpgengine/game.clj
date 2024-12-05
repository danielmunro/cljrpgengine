(ns cljrpgengine.game
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.item :as item]
            [cljrpgengine.screens.main-menu :as main-menu])
  (:import [com.badlogic.gdx Game]
           (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Color OrthographicCamera)
           (com.badlogic.gdx.graphics.g2d SpriteBatch)
           (com.badlogic.gdx.graphics.g2d.freetype FreeTypeFontGenerator FreeTypeFontGenerator$FreeTypeFontParameter)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer)
           (com.badlogic.gdx.utils.viewport FitViewport)))

(gen-class
 :name "cljrpgengine.game.Game"
 :extends com.badlogic.gdx.Game)

(defn -create [^Game this]
  (swap! deps/batch (constantly (SpriteBatch.)))
  (swap! deps/camera (constantly (OrthographicCamera.)))
  (swap! deps/viewport (constantly (FitViewport. (/ constants/screen-width constants/tile-size)
                                                 (/ constants/screen-height constants/tile-size)
                                                 @deps/camera)))
  (swap! deps/shape (constantly (ShapeRenderer.)))
  (let [generator (FreeTypeFontGenerator. (FileHandle. constants/font-file))
        parameters (FreeTypeFontGenerator$FreeTypeFontParameter.)]
    (set! (. parameters -size) constants/font-size)
    (set! (. parameters -color) Color/WHITE)
    (swap! deps/font (constantly (.generateFont generator parameters))))

  (item/load-items!)

  (.setScreen this (main-menu/screen this)))

(defn -dispose [^Game _]
  (.dispose @deps/batch)
  (.dispose @deps/font))
