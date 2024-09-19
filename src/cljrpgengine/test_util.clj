(ns cljrpgengine.test-util
  (:require [cljrpgengine.mob :as mob]
            [cljrpgengine.state :as state]))

(defn create-test-player
  [x y direction]
  {:party [(mob/create-mob :test-mob "test mob" direction x y nil)]})

(defn create-new-state
  [x y direction]
  (ref
   (merge
    state/initial-state
    {:player (create-test-player x y direction)})))
