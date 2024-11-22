(ns cljrpgengine.screens.main-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.ui2 :as ui2]
            [cljrpgengine.screens.dungeon :as dungeon])
  (:import (com.badlogic.gdx Gdx Screen)
           [com.badlogic.gdx.graphics Color]
           (com.badlogic.gdx.scenes.scene2d Group Stage)
           (com.badlogic.gdx.scenes.scene2d.ui Label Label$LabelStyle)
           (com.badlogic.gdx.utils ScreenUtils)))

(defn screen
  [game]
  (let [stage (atom nil)
        dispose (fn []
                  (.dispose @deps/batch)
                  (.dispose @deps/font))]
    (proxy [Screen] []

      (show []
        (reset! stage (Stage.))
        (let [group (Group.)
              window (ui2/create-window 0 0 constants/screen-width constants/screen-height)
              label (ui2/create-label "Chronicles of Telam")]

          (.addActor group window)

          (doto label
            (.setFontScale 2.0)
            (.setX (- (/ constants/screen-width 2) (.getWidth label)))
            (.setY (* constants/screen-height 2/3)))
          (.addActor group label)

          (.addActor @stage group)))

      (render [delta]
        (ScreenUtils/clear Color/BLACK)
        (.apply deps/viewport)
        (doto @stage
          (.act delta)
          (.draw))
        (if (.isTouched Gdx/input)
          (.setScreen game (dungeon/screen game))))

      (dispose []
        (dispose))

      (hide []
        #_(.setInputProcessor Gdx/input nil))

      (pause [])

      (resize [w h]
        (.update deps/viewport w h true))

      (resume []))))
