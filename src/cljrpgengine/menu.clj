(ns cljrpgengine.menu
  (:require [cljrpgengine.ui :as ui])
  (:import (com.badlogic.gdx.scenes.scene2d Group)))

(def opened-menus (atom []))
(def menu-group (doto (Group.)))

(defn add-menu!
  [menu]
  (swap! opened-menus conj menu)
  (.addActor menu-group (:actor menu)))

(defn remove-menu!
  ([]
   (let [menu (last @opened-menus)]
     (swap! opened-menus pop)
     (.removeActor menu-group (:actor menu))))
  ([count]
   (doseq [_ (range 0 count)]
     (remove-menu!))))

(defn create-option
  [label on-selected]
  {:label label
   :on-selected on-selected})

(defn create-menu
  ([identifier window options on-change]
   (let [cursor (ui/create-cursor)
         group (doto (proxy [Group] []
                       (act [_]
                         (let [actor (:actor cursor)
                               selected (:label (nth options @(:index cursor)))]
                           (.setX actor (- (.getX selected) (.getWidth actor) 5))
                           (.setY actor (- (.getY selected) (/ (- (.getHeight actor) (.getHeight selected)) 2))))))
                 (.setX 0)
                 (.setY 0)
                 (.setWidth (.getWidth window))
                 (.setHeight (.getHeight window)))]
     (.addActor group window)
     (doseq [o options]
       (.addActor window (:label o)))
     (.addActor window (:actor cursor))
     {:identifier identifier
      :cursor cursor
      :options options
      :actor group
      :on-change on-change}))
  ([identifier window options]
   (create-menu identifier window options (fn [_]))))

(defn inc-cursor-index!
  [menu]
  (let [index (-> menu :cursor :index)]
    (if (< @index (dec (count (:options menu))))
      (swap! index inc)
      (swap! index (constantly 0)))
    ((:on-change menu) @index)))

(defn dec-cursor-index!
  [menu]
  (let [index (-> menu :cursor :index)]
    (if (= 0 @index)
      (swap! index (constantly (dec (count (:options menu)))))
      (swap! index dec))
    ((:on-change menu) @index)))

(defn option-selected
  [menu]
  (let [option (nth (:options menu) @(-> menu :cursor :index))]
    ((:on-selected option))))
