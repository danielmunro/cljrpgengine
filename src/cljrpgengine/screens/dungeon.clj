(ns cljrpgengine.screens.dungeon
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.mob :as mob])
  (:import (com.badlogic.gdx Gdx Input$Keys InputAdapter Screen)
           (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx.maps.tiled TmxMapLoader)
           (com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer)
           (com.badlogic.gdx.scenes.scene2d Stage)
           (com.badlogic.gdx.utils Array ScreenUtils)))

(defn screen
  [_]
  (let [stage (atom nil)
        dispose (fn []
                  (.dispose @deps/batch)
                  (.dispose @deps/font))
        {:keys [actor do-move! stop-move! x y]} (mob/create-mob "edwyn.png")
        tiledmap (.load (TmxMapLoader.) "resources/scenes/tinytown/main/tinytown-main.tmx")
        renderer (OrthogonalTiledMapRenderer. tiledmap (float 1/16) @deps/batch)]
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
                :else false))))
        (.setToOrtho @deps/camera
                     false
                     (/ constants/screen-width constants/tile-size)
                     (/ constants/screen-height constants/tile-size))
        (.setView renderer @deps/camera))
      (render [delta]
        (.set (. @deps/camera position)
              @x
              @y
              0)
        (.update @deps/camera)
        (.setProjectionMatrix @deps/batch (.-combined @deps/camera))
        (ScreenUtils/clear Color/BLACK)
        (.begin @deps/batch)
        (.renderTileLayer renderer (.get (.getLayers tiledmap) "background"))
        (.renderTileLayer renderer (.get (.getLayers tiledmap) "midground"))
        (.end @deps/batch)
        (doto @stage
          (.act delta)
          (.draw))
        (.begin @deps/batch)
        (.renderTileLayer renderer (.get (.getLayers tiledmap) "foreground"))
        (.end @deps/batch))
      (dispose []
        (dispose))
      (hide [])
      (pause [])
      (resize [_ _])
      (resume []))))
