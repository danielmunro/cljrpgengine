(ns cljrpgengine.menu
  (:require [cljrpgengine.ui :as ui])
  (:import (com.badlogic.gdx.scenes.scene2d Actor Group)))

(defn create-option
  [label on-selected]
  #_{:x x
   :y y
   :label label
   :on-selected on-selected}
  #_(.addListener label on-selected))

(defn create-window
  [x y width height]
  {:x x
   :y y
   :width width
   :height height})

(defn create-menu
  [identifier window options]
  (let [cursor (ui/create-cursor)
        group (proxy [Group] []
                (act [_]
                  (let [actor (:actor cursor)
                        selected (nth options @(:index cursor))]
                    (.setX actor (.getX selected))
                    (.setY actor (.getY selected)))))]
    (.addActor group window)
    (doseq [o options]
      (.addActor group o))
    (.addActor group (:actor cursor))
    {:identifier identifier
     :cursor cursor
     :options options
     :actor group}))
