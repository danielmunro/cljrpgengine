(ns cljrpgengine.menus.main.main-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menus.main.select-game :as select-game]
            [cljrpgengine.initialize-game :as initialize-game]
            [cljrpgengine.ui :as ui]
            [clojure.java.io :as io]))

(def last-save (.exists (io/as-file (str constants/save-dir "last-save.txt"))))

(def initial-menu-items '("New Game"
                          "Load Game"
                          "Settings"
                          "Quit"))

(def final-menu-items (if last-save
                        (conj initial-menu-items "Continue")
                        initial-menu-items))

(deftype MainMenu [state]
  menu/Menu
  (draw [menu]
    (let [padding-x (/ constants/screen-width 10)
          padding-y (/ constants/screen-height 10)
          width (- constants/screen-width (* padding-x 2))
          height (- constants/screen-height (* padding-y 2))
          menu-item (partial ui/draw-line padding-x padding-y)
          cursor (ui/get-menu-cursor (.menu-type menu))]
      (ui/draw-window padding-x padding-y width height)
      (ui/draw-cursor padding-x padding-y cursor)
      (dorun
       (for [i (range (count final-menu-items))]
         (menu-item i (nth final-menu-items i))))))
  (cursor-length [_] (count final-menu-items))
  (menu-type [_] :main-menu)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor (.menu-type menu))]
      (cond
        (= (nth final-menu-items cursor) "Continue")
        (initialize-game/load-save state "last-save.txt")
        (= (nth final-menu-items cursor) "New Game")
        (initialize-game/start state)
        (= (nth final-menu-items cursor) "Load Game")
        (ui/open-menu! (select-game/create-menu state))
        (= (nth final-menu-items cursor) "Settings")
        (println "settings")
        (= (nth final-menu-items cursor) "Quit")
        (System/exit 0)))))

(defn create-menu
  [state]
  (MainMenu. state))
