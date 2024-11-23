(ns cljrpgengine.screens.dungeon
  (:require [cljrpgengine.deps :as deps]
            [cljrpgengine.mob :as mob])
  (:import (com.badlogic.gdx Screen)
           (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx.scenes.scene2d Stage)
           (com.badlogic.gdx.utils ScreenUtils)))

(defn screen
  [_]
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
