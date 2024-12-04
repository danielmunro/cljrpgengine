(ns cljrpgengine.screens.dungeon
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.input-adapter :as input-adapter]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.menu.party :as party-menu]
            [cljrpgengine.tilemap :as tilemap]
            [clojure.java.io :as io]
            [cljrpgengine.util :as util])
  (:import (com.badlogic.gdx Gdx Screen)
           (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer)
           (com.badlogic.gdx.scenes.scene2d Group Stage)
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

(def keys-typed (atom #{}))

(defn- key-typed!
  [key]
  (swap! keys-typed conj key)
  false)

(defn- load-mobs
  [scene room]
  (let [file-path (str constants/scenes-dir (name scene) "/" (name room) "/mobs")
        dir (io/file file-path)
        mobs (atom nil)]
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
                               (/ (first coords) constants/tile-size)
                               (- (dec (.get (.getProperties @tilemap/tilemap) "height")) (/ (second coords) constants/tile-size))
                               animation))))))
    @mobs))

(defn screen
  [game scene room entrance-name]
  (tilemap/load-tilemap scene room)
  (let [stage (Stage. @deps/viewport @deps/batch)
        menu-stage (Stage.)
        mob-group (Group.)
        entrance (tilemap/get-entrance entrance-name)
        mobs (load-mobs scene room)
        {:keys [actor
                key-down!
                key-up!
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
        sort-actors (fn []
                      (let [sorted (sort
                                    (fn [a b]
                                      (cond
                                        (= (.getY a) (.getY b))
                                        0
                                        (> (.getY a) (.getY b))
                                        -1
                                        :else
                                        1))
                                    (.getChildren mob-group))]
                        (doseq [i (range 0 (.count sorted))]
                          (.setZIndex (nth sorted i) i))))
        do-move! (fn [next-x next-y to-x to-y delta]
                   (when (or (not (on-tile (.getX actor) (.getY actor)))
                             (and (not (tilemap/is-blocked? next-x next-y))
                                  (not (.hit mob-group next-x next-y true))))
                     (.setX actor to-x)
                     (.setY actor to-y)
                     (swap! moving (constantly true))
                     (add-time-delta! delta)
                     (sort-actors)))
        evaluate-direction-moving! (fn [direction delta]
                                     (let [x (.getX actor)
                                           y (.getY actor)]
                                       (case direction
                                         :up
                                         (do-move! x (inc y) x (util/round1 (+ y MOVE_AMOUNT)) delta)
                                         :down
                                         (do-move! x (dec y) x (util/round1 (- y MOVE_AMOUNT)) delta)
                                         :left
                                         (do-move! (dec x) y (util/round1 (- x MOVE_AMOUNT)) y delta)
                                         :right
                                         (do-move! (inc x) y (util/round1 (+ x MOVE_AMOUNT)) y delta))))
        evaluate-on-tile! (fn [delta]
                            (if-let [{:keys [scene room to]} (tilemap/get-exit (.getX actor) (.getY actor))]
                              (.setScreen game (screen game scene room to))
                              (if-let [key (first @keys-down)]
                                (when (is-direction? key)
                                  (evaluate-direction-moving! key delta)
                                  (swap! direction (constantly key)))
                                (when @moving
                                  (swap! moving (constantly false))
                                  (swap! state-time (constantly 0))))))
        evaluate-movement! (fn [delta]
                             (cond
                               (not-empty @menu/opened-menus)
                               nil
                               (on-tile (.getX actor) (.getY actor))
                               (evaluate-on-tile! delta)
                               :else
                               (evaluate-direction-moving! @direction delta)))
        evaluate-input! (fn []
                          (when-let [key (first @keys-typed)]
                            (case key
                              :c
                              (println (.getX actor) (.getY actor))
                              :m
                              (if (empty? @menu/opened-menus)
                                (menu/add-menu! (party-menu/create)))
                              :q
                              (if-not (empty? @menu/opened-menus)
                                (menu/remove-menu!))
                              :up
                              (if-let [menu (last @menu/opened-menus)]
                                (menu/dec-cursor-index! menu))
                              :down
                              (if-let [menu (last @menu/opened-menus)]
                                (menu/inc-cursor-index! menu))
                              :left
                              (if-let [_ (last @menu/opened-menus)]
                                (println "menu left not implemented"))
                              :right
                              (if-let [_ (last @menu/opened-menus)]
                                (println "menu right not implemented"))
                              :space
                              (if-let [menu (last @menu/opened-menus)]
                                (menu/option-selected menu))
                              false)
                            (swap! keys-typed disj key)))
        dispose (fn []
                  (.dispose stage)
                  (.dispose renderer))
        update-camera (fn []
                        (let [t (* constants/tile-size 2)]
                          (.set (. @deps/camera position)
                                (+ (.getX actor) (/ constants/mob-width t))
                                (+ (.getY actor) (/ constants/mob-height t))
                                0))
                        (.update @deps/camera)
                        (.setView renderer @deps/camera)
                        (.setProjectionMatrix @deps/batch (.-combined @deps/camera)))
        draw (fn [delta]
               (ScreenUtils/clear Color/BLACK)
               (.begin @deps/batch)
               (.renderTileLayer renderer (tilemap/get-layer tilemap/LAYER_BACKGROUND))
               (.renderTileLayer renderer (tilemap/get-layer tilemap/LAYER_MIDGROUND))
               (.end @deps/batch)
               (doto stage
                 (.act delta)
                 (.draw))
               (.begin @deps/batch)
               (.renderTileLayer renderer (tilemap/get-layer tilemap/LAYER_FOREGROUND))
               (.end @deps/batch)
               (doto menu-stage
                 (.act delta)
                 (.draw)))]
    (proxy [Screen] []
      (show []
        (doseq [mob (vals mobs)]
          (.addActor mob-group (:actor mob)))
        (.addActor mob-group actor)
        (.addActor stage mob-group)
        (.addActor menu-stage menu/menu-group)
        (.setInputProcessor
         Gdx/input
         (input-adapter/dungeon-input-adapter key-down! key-up! key-typed!))
        (.setToOrtho @deps/camera
                     false
                     (/ constants/screen-width constants/tile-size)
                     (/ constants/screen-height constants/tile-size)))
      (render [delta]
        (update-camera)

        (draw delta)

        (evaluate-movement! delta)

        (evaluate-input!))
      (dispose []
        (dispose))
      (hide []
        (dispose))
      (pause [])
      (resize [width height]
        (.update @deps/viewport width height true))
      (resume []))))
