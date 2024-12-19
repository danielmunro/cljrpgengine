(ns cljrpgengine.menu
  (:require [cljrpgengine.deps :as deps]
            [cljrpgengine.ui :as ui]
            [clojure.math :as math])
  (:import (com.badlogic.gdx.scenes.scene2d Group)
           (com.badlogic.gdx.scenes.scene2d.ui ScrollPane)))

(def opened-menus (atom []))
(def menu-group (doto (Group.)))

(defn create-event
  [event-type changed]
  {:event-type event-type
   :changed changed})

(defn add-menu!
  [menu]
  (swap! opened-menus conj menu)
  (.addActor menu-group (:window menu)))

(defn remove-menu!
  ([]
   (let [menu (last @opened-menus)]
     (swap! opened-menus pop)
     (.removeActor menu-group (:window menu))
     (if-let [m (last @opened-menus)]
       ((:on-change m) (create-event :focus nil)))))
  ([count]
   (doseq [_ (range 0 count)]
     (remove-menu!))))

(defn create-option
  [label on-selected]
  {:label label
   :on-selected on-selected})

(defn create-option-group
  [options width]
  (let [cursor (ui/create-cursor)
        option-group (doto (proxy [Group] []
                             (act [_]
                               (let [actor (:image cursor)
                                     selected (:label (nth options @(:index cursor)))]
                                 (.setX actor (- (.getX selected) (.getWidth actor) 5))
                                 (.setY actor (- (.getY selected) (/ (- (.getHeight actor) (.getHeight selected)) 2))))))
                       (.setX 0)
                       (.setY 0)
                       (.setWidth width)
                       (.setHeight (reduce (fn [a b] (+ a (.getHeight (:label b)))) 0 options))
                       (.addActor (:image cursor)))]
    (doseq [option options]
      (.addActor option-group (:label option)))
    {:option-group option-group
     :cursor cursor}))

(defn scrollable
  [option-group x y width height]
  (doto (ScrollPane. option-group)
    (.setX x)
    (.setY y)
    (.setWidth width)
    (.setHeight height)))

(defn create-menu
  ([identifier window scroll-pane options cursor on-change]
   {:identifier identifier
    :window window
    :scroll-pane scroll-pane
    :options options
    :on-change on-change
    :cursor cursor})
  ([identifier window options on-change]
   (let [{:keys [option-group cursor]} (create-option-group
                                        options
                                        (.getWidth window))
         scroll-pane (scrollable option-group
                                 0
                                 0
                                 (.getWidth window)
                                 (.getHeight window))]
     (.addActor window scroll-pane)
     (.addActor window (:image cursor))
     (create-menu identifier window scroll-pane options cursor on-change)))
  ([identifier window options]
   (create-menu identifier window options (fn [_]))))

(defn update-scroll
  [scroll-pane index]
  (let [lines-shown (math/floor (/ (.getHeight scroll-pane)
                                   (.getLineHeight @deps/font)))
        lowest-line (math/ceil (/ (.getScrollY scroll-pane)
                                  (.getLineHeight @deps/font)))
        highest-line (math/floor (dec (+ lines-shown (/ (.getScrollY scroll-pane)
                                                        (.getLineHeight @deps/font)))))]
    (cond
      (< index lowest-line)
      (.setScrollY scroll-pane (* index (.getLineHeight @deps/font)))
      (< highest-line index)
      (.setScrollY scroll-pane (- (* (inc index) (.getLineHeight @deps/font))
                                  (.getHeight scroll-pane))))))

(defn inc-cursor-index!
  [menu]
  (let [index (-> menu :cursor :index)]
    (if (< @index (dec (count (:options menu))))
      (swap! index inc)
      (swap! index (constantly 0)))
    ((:on-change menu) (create-event :cursor @index))
    (update-scroll (:scroll-pane menu) @index)))

(defn dec-cursor-index!
  [menu]
  (let [index (-> menu :cursor :index)]
    (if (= 0 @index)
      (swap! index (constantly (dec (count (:options menu)))))
      (swap! index dec))
    ((:on-change menu) (create-event :cursor @index))
    (update-scroll (:scroll-pane menu) @index)))

(defn option-selected
  [menu]
  (let [option (nth (:options menu) @(-> menu :cursor :index))]
    ((:on-selected option))))
