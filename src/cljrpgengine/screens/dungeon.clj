(ns cljrpgengine.screens.dungeon
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.input-adapter :as input-adapter]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.tilemap :as tilemap]
            [clojure.java.io :as io])
  (:import (com.badlogic.gdx Gdx Screen)
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

(def keys-typed (atom #{}))

(defn- key-typed!
  [key]
  (swap! keys-typed conj key)
  false)

(def mobs (atom nil))

(defn- load-mobs!
  [scene room]
  (let [file-path (str constants/scenes-dir (name scene) "/" (name room) "/mobs")
        dir (io/file file-path)]
    (swap! mobs (constantly nil))
    (if (.exists dir)
      (let [mob-files (.listFiles dir)]
        (doseq [mob-file mob-files]
          (let [{:keys [identifier name direction coords animation]}
                (read-string (slurp (str file-path "/" (.getName mob-file))))]
            (swap! mobs assoc
                   identifier (mob/create-mob
                                identifier
                                name
                                direction
                                (first coords)
                                (second coords)
                                animation))))))))

(defn screen
  [game scene room entrance-name]
  (tilemap/load-tilemap scene room)
  (let [stage (atom nil)
        entrance (tilemap/get-entrance entrance-name)
        {:keys [actor
                key-down!
                key-up!
                x
                y
                direction
                keys-down
                add-time-delta!
                state-time]} (mob/create-mob
                                       :edwyn
                                       "Edwyn"
                                       (:direction entrance)
                                       (int (:x entrance))
                                       (int (:y entrance))
                                       :edwyn)
        renderer (OrthogonalTiledMapRenderer. @tilemap/tilemap (float (/ 1 constants/tile-size)) @deps/batch)
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
        evaluate-movement! (fn [delta]
                             (if (on-tile @x @y)
                               (evaluate-on-tile! delta)
                               (move! x y @direction add-time-delta! delta)))
        evaluate-key-pressed! (fn []
                                (when-let [key (first @keys-typed)]
                                  (case key
                                    :c
                                    (println @x @y))
                                  (swap! keys-typed disj key)))
        evaluate! (fn [delta]
                    (evaluate-movement! delta)
                    (evaluate-key-pressed!))
        dispose (fn []
                  (.dispose @stage)
                  (.dispose renderer))]
    (proxy [Screen] []
      (show []
        (reset! stage (Stage.))
        (load-mobs! scene room)
        (doseq [mob (vals @mobs)]
          (.addActor @stage (:actor mob)))
        ;(.addActor @stage actor)
        (.setInputProcessor
         Gdx/input
         (input-adapter/create-input-adapter key-down! key-up! key-typed!))
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
      (hide []
        (dispose))
      (pause [])
      (resize [_ _])
      (resume []))))
