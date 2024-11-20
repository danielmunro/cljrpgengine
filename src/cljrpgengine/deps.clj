(ns cljrpgengine.deps
  (:import (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)
           (com.badlogic.gdx.utils.viewport FitViewport)))

(def viewport (FitViewport. 8 5))
;(def batch (SpriteBatch.))
;(def font (BitmapFont.))
(def batch (atom nil))
(def font (atom nil))
