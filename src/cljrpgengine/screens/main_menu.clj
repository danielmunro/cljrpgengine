(ns cljrpgengine.screens.main-menu
  (:require [cljrpgengine.deps :as deps])
  (:import (com.badlogic.gdx Gdx Screen)
           [com.badlogic.gdx.graphics Color GL20]
           (com.badlogic.gdx.graphics.g2d BitmapFont)
           (com.badlogic.gdx.scenes.scene2d Stage)
           (com.badlogic.gdx.scenes.scene2d.ui Label Label$LabelStyle)
           (com.badlogic.gdx.utils ScreenUtils)))

(defn screen
  [game]
  (let [stage (atom nil)
        ;menu (cljrpgengine.menus.main.main-menu/create-menu)
        dispose (fn []
                  (println "in dispose")
                  (.dispose @deps/batch)
                  (.dispose @deps/font))]
    (proxy [Screen] []
      (show []
        (println "show")
        (reset! stage (Stage.))
        (let [style (Label$LabelStyle. (BitmapFont.) (Color. 1 1 1 1))
              label (Label. "Hello world!" style)]
          (.addActor @stage label))
        )
      (render [delta]
        ;(.glClearColor (Gdx/gl) 0 0 0 0)
        ;(.glClear (Gdx/gl) GL20/GL_COLOR_BUFFER_BIT)
        (ScreenUtils/clear Color/BLACK)
        (.apply deps/viewport)

        ;(.setProjectionMatrix @deps/batch (.combined (.getCamera deps/viewport)))
        ;(.begin @deps/batch)
        ;(.draw @deps/font @deps/batch "Welcome to my game" (float 1.0) (float 1.5))
        ;(.draw @deps/font @deps/batch "Tap anywhere yo" (float 1.0) (float 1.0))
        ;(.end @deps/batch)
        ;(if (.isTouched Gdx/input)
        ;  (dispose))

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
