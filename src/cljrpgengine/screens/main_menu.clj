(ns cljrpgengine.screens.main-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.screens.dungeon :as dungeon])
  (:import (com.badlogic.gdx Gdx Screen)
           [com.badlogic.gdx.graphics Color]
           (com.badlogic.gdx.scenes.scene2d Group Stage)
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
              window (ui/create-window 0 0 constants/screen-width constants/screen-height)
              label-title (ui/create-label "Chronicles of Telam")]

          (doto label-title
            (.setFontScale 2.0)
            (.setX (- (/ constants/screen-width 2) (.getWidth label-title)))
            (.setY (* constants/screen-height 2/3)))

          (.addActor group window)
          (.addActor group label-title)
          (.addActor @stage group))

        (swap! player/party
               (constantly [(mob/create-mob
                             :edwyn
                             "Edwyn"
                             :down
                             0
                             0
                             :edwyn)
                            (mob/create-mob
                             :dudelgor
                             "Dudelgor"
                             :down
                             0
                             0
                             :cyrus)]))
        (player/add-item! :light-health-potion)
        (player/add-item! :light-health-potion)
        (player/add-item! :practice-sword))

      (render [delta]
        (ScreenUtils/clear Color/BLACK)
        (.update @deps/camera)
        (doto @stage
          (.act delta)
          (.draw))
        (if (.isTouched Gdx/input)
          (.setScreen game (dungeon/screen
                            game
                            :tinytown
                            :main
                            :start))))

      (dispose []
        (dispose))

      (hide [])

      (pause [])

      (resize [w h]
        (set! (. @deps/camera -viewportWidth) (float 100))
        (set! (. @deps/camera -viewportHeight) (* (float 100) (/ h w)))
        (.update @deps/camera))

      (resume []))))
