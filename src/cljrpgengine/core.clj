(ns cljrpgengine.core
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.log :as log])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication LwjglApplicationConfiguration]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(def save-file (atom nil))

(defn- parse-args
  [args]
  (if (seq args)
    (doseq [arg args]
      (cond
        (= "-s" arg)
        (swap! save-file (constantly (first (next args))))
        (= "-l" arg)
        (swap! log/log-level (constantly (keyword (first (next args)))))))))

(defn- get-configuration
  []
  (let [config (LwjglApplicationConfiguration.)]
    (set! (. config -title) "demo")
    (set! (. config -width) constants/screen-width)
    (set! (. config -height) constants/screen-height)
    config))

(defn -main
  "Start the game."
  [& args]
  (parse-args args)
  ;(setup)
  (LwjglApplication. (cljrpgengine.game.Game.) (get-configuration))
  (Keyboard/enableRepeatEvents true))
