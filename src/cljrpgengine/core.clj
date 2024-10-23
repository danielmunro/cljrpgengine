(ns cljrpgengine.core
  (:require [cljrpgengine.sprite :as sprite]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.state :as state]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.input :as input]
            [cljrpgengine.effect :as effect]
            [cljrpgengine.game-loop :as game-loop]
            [cljrpgengine.window :as window]
            [cljrpgengine.log :as log]
            [cljrpgengine.scenes.main-menu-scene :as main-menu-scene])
  (:gen-class))

(def save-file (atom nil))

(defn setup-state!
  "Setup function for the game."
  []
  (ui/init!)
  (sprite/load-sprites)
  (let [state (state/create-new-state)
        scene (main-menu-scene/create state)]
    (window/create
     constants/screen-width
     constants/screen-height
     #(input/key-pressed! state %)
     #(input/key-released! state %))
    (dosync (alter state assoc :scene scene))
    (.initialize-scene scene)
    (effect/add-fade-in state)
    state))

(defn -main
  "Start the game."
  [& args]
  (if (seq args)
    (doseq [arg args]
      (cond
        (= "-s" arg)
        (swap! save-file (constantly (first (next args))))
        (= "-l" arg)
        (swap! log/log-level (constantly (keyword (first (next args))))))))
  (log/info "starting game...")
  (game-loop/run (setup-state!)))
