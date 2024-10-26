(ns cljrpgengine.create-menu
  (:require [cljrpgengine.menus.main.main-menu :as main-menu]))

(defn create
  [state menu]
  (cond
    (= menu :main-menu)
    (main-menu/create-menu state)))