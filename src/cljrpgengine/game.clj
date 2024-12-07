(ns cljrpgengine.game
  (:require [cljrpgengine.deps :as deps]
            [cljrpgengine.item :as item]
            [cljrpgengine.screens.main-menu :as main-menu])
  (:import [com.badlogic.gdx Game]))

(gen-class
 :name "cljrpgengine.game.Game"
 :extends com.badlogic.gdx.Game)

(defn -create [^Game this]
  (deps/init-dependencies)

  (item/load-items!)

  (.setScreen this (main-menu/screen this)))

(defn -dispose [^Game _]
  (.dispose @deps/batch)
  (.dispose @deps/font))
