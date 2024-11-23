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
  (swap! deps/camera (constantly (OrthographicCamera. constants/screen-width constants/screen-height)))
  (.setScreen this (main-menu/screen this)))

(defn -dispose [^Game _]
  (.dispose @deps/batch)
  (.dispose @deps/font))
