(ns cljrpgengine.menus.main.select-game
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menus.main.select-save :as select-save]
            [cljrpgengine.ui :as ui]
            [clojure.java.io :as io]))

(def saves-loaded (atom false))
(def saves (atom {}))

(defn- get-saves
  []
  (if (not @saves-loaded)
    (let [save-dir (io/file constants/save-dir)
          files (filter #(not= "last-save.txt" %) (seq (.list save-dir)))
          save-files (map (fn [f] {:name f
                                   :saves (seq (.list (io/file (str constants/save-dir f))))}) files)]
      (swap! saves-loaded (constantly true))
      (swap! saves (constantly save-files))))
  @saves)

(deftype SelectGameMenu [state]
  menu/Menu
  (draw [menu]
    (let [padding-x (/ constants/screen-width 10)
          padding-y (/ constants/screen-height 10)
          width (- constants/screen-width (* padding-x 2))
          height (- constants/screen-height (* padding-y 2))
          menu-item (partial ui/draw-line padding-x padding-y)
          saves (get-saves)
          cursor (ui/get-menu-cursor (.menu-type menu))]
      (ui/draw-window padding-x padding-y width height)
      (ui/draw-cursor padding-x padding-y cursor)
      (dorun
       (for [s (range (count saves))]
         (menu-item s (:name (nth saves s)))))))
  (cursor-length [_]
    (count (get-saves)))
  (menu-type [_] :select-game-menu)
  (key-pressed [menu]
    (ui/open-menu!
     (select-save/create-menu
      state
      (nth (get-saves) (ui/get-menu-cursor (.menu-type menu)))))))

(defn create-menu
  [state]
  (SelectGameMenu. state))
