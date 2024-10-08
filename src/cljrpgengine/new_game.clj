(ns cljrpgengine.new-game
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.create-scene :as create-scene]
            [cljrpgengine.map :as map]
            [cljrpgengine.player :as player]
            [cljrpgengine.state :as state]
            [cljrpgengine.ui :as ui]))

(defn start
  [state]
  (let [scene (create-scene/create state :tinytown)]
    (dosync (alter state assoc
                   :player (player/create-new-player)
                   :map (map/load-map "tinytown" "main")
                   :save-name (random-uuid)
                   :money constants/starting-money
                   :items {:light-health-potion 2
                           :light-mana-potion 1
                           :practice-sword 1}
                   :scene scene)
            (alter state dissoc :new-game))
    (.initialize-scene scene))
  (map/init-map state)
  (if (ui/is-menu-open? state)
    (ui/close-menu! state)))

(defn load-save
  [state]
  (let [file (:load-game @state)
        new-state (state/load-save-file file)
        scene (create-scene/create state (:scene @new-state))]
    (dosync (alter state merge @new-state)
            (alter state dissoc :load-game)
            (alter state assoc :scene scene))
    (.initialize-scene scene)
    (if (ui/is-menu-open? state)
      (ui/close-menu! state))))
