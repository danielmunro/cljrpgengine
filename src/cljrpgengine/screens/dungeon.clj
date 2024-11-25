(ns cljrpgengine.screens.dungeon
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.tilemap :as tilemap])
  (:import (com.badlogic.gdx Gdx Input$Keys InputAdapter Screen)
           (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer)
           (com.badlogic.gdx.scenes.scene2d Stage)
           (com.badlogic.gdx.utils ScreenUtils)))

(defn screen
  [_ scene room]
  (let [stage (atom nil)
        dispose (fn []
                  (.dispose @deps/batch)
                  (.dispose @deps/font))
        {:keys [actor key-down! key-up! x y]} (mob/create-mob "edwyn.png")
        tiled (tilemap/load-tilemap scene room)
        renderer (OrthogonalTiledMapRenderer. tiled (float 1/16) @deps/batch)]
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
                (key-down! :left)
                (= key Input$Keys/RIGHT)
                (key-down! :right)
                (= key Input$Keys/UP)
                (key-down! :up)
                (= key Input$Keys/DOWN)
                (key-down! :down)
                :else false))
            (keyUp [key]
              (cond
                (= key Input$Keys/LEFT)
                (key-up! :left)
                (= key Input$Keys/RIGHT)
                (key-up! :right)
                (= key Input$Keys/UP)
                (key-up! :up)
                (= key Input$Keys/DOWN)
                (key-up! :down)
                :else false))))
        (.setToOrtho @deps/camera
                     false
                     (/ constants/screen-width constants/tile-size)
                     (/ constants/screen-height constants/tile-size)))
      (render [delta]
        (.set (. @deps/camera position) (+ @x (/ constants/mob-width 32)) (+ @y (/ constants/mob-height 32)) 0)
        (.update @deps/camera)
        (.setView renderer @deps/camera)
        (.setProjectionMatrix @deps/batch (.-combined @deps/camera))
        (ScreenUtils/clear Color/BLACK)
        (.begin @deps/batch)
        (.renderTileLayer renderer (tilemap/get-layer tiled tilemap/LAYER_BACKGROUND))
        (.renderTileLayer renderer (tilemap/get-layer tiled tilemap/LAYER_MIDGROUND))
        (.end @deps/batch)
        (doto @stage
          (.act delta)
          (.draw))
        (.begin @deps/batch)
        (.renderTileLayer renderer (tilemap/get-layer tiled tilemap/LAYER_FOREGROUND))
        (.end @deps/batch))
      (dispose []
        (dispose))
      (hide [])
      (pause [])
      (resize [_ _])
      (resume []))))
