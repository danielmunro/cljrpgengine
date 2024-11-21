(ns cljrpgengine.deps
  (:import (com.badlogic.gdx.utils.viewport FitViewport)))

(def viewport (FitViewport. 8 5))
(def batch (atom nil))
(def font (atom nil))
