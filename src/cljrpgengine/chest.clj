(ns cljrpgengine.chest
  (:require [cljrpgengine.scene :as scene]))

(defn chest-key
  [chest]
  (str (:name @scene/scene) "-" (:room @scene/scene) "-" (:id chest)))