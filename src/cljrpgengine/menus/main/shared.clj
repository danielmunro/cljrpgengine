(ns cljrpgengine.menus.main.shared)

(defn load-game
  [state file]
  (dosync (alter state assoc :load-game file)))
