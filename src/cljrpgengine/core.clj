(ns cljrpgengine.core
  (:require [cljrpgengine.item :as item]
            [cljrpgengine.menus.main.main-menu :as main-menu]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.fight :as beast]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.input :as input]
            [cljrpgengine.effect :as effect]
            [cljrpgengine.game-loop :as game-loop]
            [cljrpgengine.window :as window]
            [cljrpgengine.log :as log])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(def save-file (atom nil))

(defn setup
  "Setup function for the game."
  []
  (ui/init!)
  (sprite/load-sprites)
  #_(window/create
   constants/screen-width
   constants/screen-height
   #(input/key-pressed! %)
   #(input/key-released! %))
  (beast/load-beastiary!)
  (item/load-items!)
  (ui/open-menu! (main-menu/create-menu))
  (effect/add-fade-in))

(defn- parse-args
  [args]
  (if (seq args)
    (doseq [arg args]
      (cond
        (= "-s" arg)
        (swap! save-file (constantly (first (next args))))
        (= "-l" arg)
        (swap! log/log-level (constantly (keyword (first (next args)))))))))

#_(defn -main
  "Start the game."
  [& args]
  (parse-args args)
  (log/info "starting game...")
  (setup)
  (game-loop/run-game!))

(defn -main
  "Start the game."
  [& args]
  (parse-args args)
  (LwjglApplication. (cljrpgengine.game.Game.) "demo" 800 600)
  (Keyboard/enableRepeatEvents true))
