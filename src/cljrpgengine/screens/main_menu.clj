(ns cljrpgengine.screens.main-menu
  (:require [cljrpgengine.deps :as deps])
  (:import (com.badlogic.gdx Gdx Screen)
           [com.badlogic.gdx.graphics Color GL20]
           (com.badlogic.gdx.graphics.g2d BitmapFont)
           (com.badlogic.gdx.scenes.scene2d Stage)
           (com.badlogic.gdx.scenes.scene2d.ui Label Label$LabelStyle)))

(defn screen
  [game]
  (println game)
  (let [stage (atom nil)]
    (proxy [Screen] []
      (show []
        (reset! stage (Stage.))
        (let [style (Label$LabelStyle. (BitmapFont.) (Color. 1 1 1 1))
              label (Label. "Hello world!" style)]
          (.addActor @stage label)))
      (render [delta]
        (.glClearColor (Gdx/gl) 0 0 0 0)
        (.glClear (Gdx/gl) GL20/GL_COLOR_BUFFER_BIT)
        (.apply deps/viewport)
        (doto @stage
          (.act delta)
          (.draw)))
      (dispose [])
      (hide [])
      (pause [])
      (resize [w h]
        (.update deps/viewport w h true))
      (resume []))))
