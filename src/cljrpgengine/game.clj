(ns cljrpgengine.game
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.screens.main-menu :as main-menu])
  (:import [com.badlogic.gdx Game]
           (com.badlogic.gdx.graphics OrthographicCamera)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)))

(gen-class
  :name "cljrpgengine.game.Game"
  :extends com.badlogic.gdx.Game)

(defn -create [^Game this]
  (swap! deps/batch (constantly (SpriteBatch.)))
  (swap! deps/font (constantly (BitmapFont.)))
  (let [camera (OrthographicCamera. constants/screen-width constants/screen-height)]
    (.set (. camera position)
          (float (/ (. camera viewportWidth) 2))
          (float (/ (. camera viewportHeight) 2))
          0)
    (.update camera)
    (swap! deps/camera (constantly camera)))
  (.setScreen this (main-menu/screen this)))

(defn -dispose [^Game _]
  (.dispose @deps/batch)
  (.dispose @deps/font))
