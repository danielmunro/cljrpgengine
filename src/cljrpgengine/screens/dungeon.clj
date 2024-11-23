(ns cljrpgengine.screens.dungeon
  (:require [cljrpgengine.deps :as deps]
            [cljrpgengine.mob :as mob])
  (:import (com.badlogic.gdx Gdx Input$Keys InputAdapter Screen)
           (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx.scenes.scene2d Stage)
           (com.badlogic.gdx.utils ScreenUtils)))

(defn screen
  [_]
  (let [stage (atom nil)
        dispose (fn []
                  (.dispose @deps/batch)
                  (.dispose @deps/font))
        {:keys [actor do-move! stop-move!]} (mob/create-mob "edwyn.png")]
    (proxy [Screen] []
      (show []
        (reset! stage (Stage.))
        (.addActor @stage actor)
        (.setInputProcessor
          Gdx/input
          (proxy [InputAdapter] []
            (keyDown [key]
              (cond
                (= key Input$Keys/LEFT)
                (do-move! :left)
                (= key Input$Keys/RIGHT)
                (do-move! :right)
                (= key Input$Keys/UP)
                (do-move! :up)
                (= key Input$Keys/DOWN)
                (do-move! :down)
                :else false))
            (keyUp [key]
              (cond
                (= key Input$Keys/LEFT)
                (stop-move! :left)
                (= key Input$Keys/RIGHT)
                (stop-move! :right)
                (= key Input$Keys/UP)
                (stop-move! :up)
                (= key Input$Keys/DOWN)
                (stop-move! :down)
                :else false)))))
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
