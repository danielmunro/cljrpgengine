(ns cljrpgengine.state
  (:require [cljrpgengine.player :as player]))

(defn create-state []
  (ref {:keys #{}
        :player (player/create-player)}))
