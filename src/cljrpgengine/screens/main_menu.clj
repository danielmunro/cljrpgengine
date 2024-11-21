(ns cljrpgengine.screens.main-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.ui2 :as ui2])
  (:import (com.badlogic.gdx Screen)
           [com.badlogic.gdx.graphics Color]
           (com.badlogic.gdx.scenes.scene2d Stage)
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
        (.addActor @stage (ui2/create-window 0 0 constants/screen-width constants/screen-height))

        #_(let [style (Label$LabelStyle. @deps/font (Color. 1 1 1 1))
              label (Label. "Hello world!" style)]
          (.setX label 100)
          (.setY label 100)
          (.addActor @stage label))
        )
      (render [delta]
         (ScreenUtils/clear Color/BLACK)
        (.apply deps/viewport)
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
