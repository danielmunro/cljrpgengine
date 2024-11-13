(ns cljrpgengine.core
  (:require [cljrpgengine.item :as item]
            [cljrpgengine.menus.main.main-menu :as main-menu]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.fight :as beast]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.state :as state]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.input :as input]
            [cljrpgengine.effect :as effect]
            [cljrpgengine.game-loop :as game-loop]
            [cljrpgengine.window :as window]
            [cljrpgengine.log :as log])
  (:gen-class))

(def save-file (atom nil))

(defn setup-state!
  "Setup function for the game."
  []
  (ui/init!)
  (sprite/load-sprites)
  (let [state (state/create-new-state)]
    (window/create
     constants/screen-width
     constants/screen-height
     #(input/key-pressed! state %)
     #(input/key-released! state %))
    (beast/load-beastiary!)
    (item/load-items!)
    (ui/open-menu! (main-menu/create-menu state))
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
  (game-loop/run-game! (setup-state!)))
