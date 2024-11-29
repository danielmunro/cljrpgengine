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

(def MOVE_AMOUNT 1/7)

(def moving (atom false))

(defn- is-direction?
  [key]
  (or (= :up key)
      (= :down key)
      (= :left key)
      (= :right key)))

(defn- on-tile
  [x y]
  (and (= (float x) (Math/ceil x))
       (= (float y) (Math/ceil y))))

(defn- move!
  [x y direction add-time-delta! delta]
  (case direction
    :up
    (when (or (not (on-tile @x @y))
              (not (tilemap/is-blocked? [@x (inc @y)])))
      (swap! y #(+ % MOVE_AMOUNT))
      (swap! moving (constantly true))
      (add-time-delta! delta))
    :down
    (when (or (not (on-tile @x @y))
              (not (tilemap/is-blocked? [@x (dec @y)])))
      (swap! y #(- % MOVE_AMOUNT))
      (swap! moving (constantly true))
      (add-time-delta! delta))
    :left
    (when (or (not (on-tile @x @y))
              (not (tilemap/is-blocked? [(dec @x) @y])))
      (swap! x #(- % MOVE_AMOUNT))
      (swap! moving (constantly true))
      (add-time-delta! delta))
    :right
    (when (or (not (on-tile @x @y))
              (not (tilemap/is-blocked? [(inc @x) @y])))
      (swap! x #(+ % MOVE_AMOUNT))
      (swap! moving (constantly true))
      (add-time-delta! delta))))

(defn screen
  [game scene room entrance-name]
  (tilemap/load-tilemap scene room)
  (let [stage (atom nil)
        {:keys [actor
                key-down!
                key-up!
                x
                y
                direction
                keys-down
                add-time-delta!
                state-time]} (mob/create-mob "edwyn.png")
        renderer (OrthogonalTiledMapRenderer. @tilemap/tilemap (float (/ 1 constants/tile-size)) @deps/batch)
        entrance (tilemap/get-entrance entrance-name)
        evaluate-on-tile! (fn [delta]
                            (if-let [{:keys [scene room to]} (tilemap/get-exit @x @y)]
                              (.setScreen game (screen game scene room to))
                              (if-let [key (first @keys-down)]
                                (when (is-direction? key)
                                  (move! x y key add-time-delta! delta)
                                  (swap! direction (constantly key)))
                                (when @moving
                                  (swap! moving (constantly false))
                                  (swap! state-time (constantly 0))))))
        evaluate! (fn [delta]
                    (if (on-tile @x @y)
                      (evaluate-on-tile! delta)
                      (move! x y @direction add-time-delta! delta)))
        dispose (fn []
                  (.dispose @stage)
                  (.dispose @deps/batch)
                  (.dispose @deps/font)
                  (.dispose renderer)
                  (.dispose @tilemap/tilemap))]
    (proxy [Screen] []
      (show []
        (swap! x (constantly (int (:x entrance))))
        (swap! y (constantly (int (:y entrance))))
        (swap! direction (constantly (:direction entrance)))
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
               (= key Input$Keys/C)
               (do (println @x @y)
                   false)
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
        (let [t (* constants/tile-size 2)]
          (.set (. @deps/camera position)
                (+ @x (/ constants/mob-width t))
                (+ @y (/ constants/mob-height t))
                0))
        (.update @deps/camera)
        (.setView renderer @deps/camera)
        (.setProjectionMatrix @deps/batch (.-combined @deps/camera))
        (.setProjectionMatrix @deps/shape (.-combined @deps/camera))
        (ScreenUtils/clear Color/BLACK)
        (.begin @deps/batch)
        (.renderTileLayer renderer (tilemap/get-layer tilemap/LAYER_BACKGROUND))
        (.renderTileLayer renderer (tilemap/get-layer tilemap/LAYER_MIDGROUND))
        (.end @deps/batch)
        (doto @stage
          (.act delta)
          (.draw))
        (.begin @deps/batch)
        (.renderTileLayer renderer (tilemap/get-layer tilemap/LAYER_FOREGROUND))
        (.end @deps/batch)
        (evaluate! delta))
      (dispose []
        (dispose))
      (hide [])
      (pause [])
      (resize [_ _])
      (resume []))))
