(ns cljrpgengine.test-util
  (:require [cljrpgengine.map :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.state :as state]))

(defn create-test-player
  [x y direction]
  {:party [(mob/create-mob :test-mob "test mob" direction x y nil)]})

(defn create-new-state
  []
  (state/create-new-state
   (create-test-player 0 0 :down)
   (map/load-map "tinytown" "main")))
