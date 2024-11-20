(ns cljrpgengine.game
  (:require [cljrpgengine.screens.main-menu :as main-menu])
  (:import [com.badlogic.gdx Game]))

(gen-class
  :name "cljrpgengine.game.Game"
  :extends com.badlogic.gdx.Game)

(defn -create [^Game this]
  (.setScreen this (main-menu/screen this)))
