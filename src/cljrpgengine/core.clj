(ns cljrpgengine.core
  (:require [cljrpgengine.create-scene :as create-scene]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.state :as state]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.input :as input]
            [cljrpgengine.effect :as effect]
            [cljrpgengine.game-loop :as game-loop]
            [cljrpgengine.window :as window])
  (:gen-class))

(def save-file (atom nil))

(defn setup-state!
  "Setup function for the game."
  []
  (ui/init!)
  (sprite/load-sprites)
  (let [state (state/create-new-state)
        scene (create-scene/create state :main-menu)
        frame (window/create
               constants/screen-width
               constants/screen-height
               #(input/key-pressed! state %)
               #(input/key-released! state %))]
    (dosync (alter state assoc
                   :scene scene
                   :buffer-strategy (.getBufferStrategy frame)))
    (.initialize-scene scene)
    (effect/add-fade-in state)
    state))

(defn -main
  "Start the game."
  [& args]
  (if (seq args)
    (doseq [arg args]
      (if (= "-s" arg)
        (swap! save-file (constantly (first (next args)))))))
  (println "starting game...")
  (game-loop/run (setup-state!)))
