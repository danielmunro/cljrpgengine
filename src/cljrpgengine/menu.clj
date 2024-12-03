(ns cljrpgengine.menu
  (:require [cljrpgengine.ui :as ui])
  (:import (com.badlogic.gdx.scenes.scene2d Group)))

(def opened-menus (atom []))
(def menu-group (Group.))

(defn add-menu!
  [menu]
  (swap! opened-menus conj menu)
  (.addActor menu-group (:actor menu)))

(defn remove-menu!
  [menu]
  (swap! opened-menus drop-last)
  (.removeActor menu-group (:actor menu)))

(defn create-option
  [label on-selected]
  {:label label
   :on-selected on-selected})

(defn create-menu
  [identifier window options]
  (let [cursor (ui/create-cursor)
        group (proxy [Group] []
                (act [_]
                  (let [actor (:actor cursor)
                        selected (:label (nth options @(:index cursor)))]
                    (.setX actor (- (.getX selected) (.getWidth actor) 5))
                    (.setY actor (- (.getY selected) (/ (- (.getHeight actor) (.getHeight selected)) 2))))))]
    (.addActor group window)
    (doseq [o options]
      (.addActor group (:label o)))
    (.addActor group (:actor cursor))
    {:identifier identifier
     :cursor cursor
     :options options
     :actor group}))

(defn inc-cursor-index!
  [menu]
  (let [index (-> menu :cursor :index)]
    (if (< @index (dec (count (:options menu))))
      (swap! index inc)
      (swap! index (constantly 0)))))

(defn dec-cursor-index!
  [menu]
  (let [index (-> menu :cursor :index)]
    (if (= 0 @index)
      (swap! index (constantly (dec (count (:options menu)))))
      (swap! index dec))))

(defn option-selected
  [menu]
  (let [option (nth (:options menu) @(-> menu :cursor :index))]
    ((:on-selected option))))
