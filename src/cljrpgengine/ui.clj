(ns cljrpgengine.ui
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.util :as util]
            [cljrpgengine.window :as window]
            [clojure.string :as str])
  (:import (java.awt Font)
           (java.io File)))

(def panel (atom nil))
(def ui-pack (atom nil))
(def main-font (atom nil))

(defn init!
  []
  (swap! panel (fn [_] (util/load-image "sprites/panel.png")))
  (swap! ui-pack (fn [_] (util/load-image "sprites/ui.png")))
  (swap! main-font (fn [_] (.deriveFont
                            (Font/createFont Font/TRUETYPE_FONT
                                             (File. (str "resources/" constants/font-family)))
                            (float constants/text-size)))))

(defn draw-line
  ([x y line-number text font-color]
   (.setColor @window/graphics (font-color constants/colors))
   (.setFont @window/graphics @main-font)
   (.drawString @window/graphics
                text
                (float (+ x constants/padding))
                (float (+ y constants/padding (* constants/line-spacing line-number)))))
  ([x y line-number text]
   (draw-line x y line-number text :font-default)))

(defn draw-window
  [x y width height]
  (let [g @window/graphics
        p @panel
        f 48
        h (/ f 2)
        q (/ h 2)]
    (.setColor g (:window constants/colors))
    (.fillRect g x y width height)

    ; upper left
    (.drawImage g p x y (+ x h) (+ y h) 0 0 h h nil)

    ; lower left
    (let [y (-> height
                (- h)
                (+ y))]
      (.drawImage g p x y (+ x h) (+ y h) 0 h h (+ h h) nil))

    ; left
    (let [y (+ y h)
          h2 (- height f)]
      (.drawImage g p x y (+ x h) (+ y h2) 0 q h (+ q h) nil))

    ; top
    (let [dx1 (+ x h)
          dx2 (-> dx1
                  (+ width)
                  (- f))]
      (.drawImage g p dx1 y dx2 (+ y h) q 0 (+ q h) h nil))

    ; upper right
    (let [dx1 (-> width
                  (- h)
                  (+ x))]
      (.drawImage g p dx1 y (+ dx1 h) (+ y h) h 0 f h nil))

    ; lower right
    (let [dx1 (-> width
                  (- h)
                  (+ x))
          dy1 (-> height
                  (- h)
                  (+ y))]
      (.drawImage g p dx1 dy1 (+ dx1 h) (+ dy1 h) h h f f nil))

    ; right
    (let [dx1 (-> width
                  (- h)
                  (+ x))
          dy1 (+ y h)
          dy2 (-> dy1
                  (+ height)
                  (- f))]
      (.drawImage g p dx1 dy1 (+ dx1 h) dy2 h q f (+ q h) nil))

    ; bottom
    (let [dx1 (+ x h)
          dy1 (-> height
                  (- h)
                  (+ y))
          dx2 (-> dx1
                  (+ width)
                  (- f))]
      (.drawImage g p dx1 dy1 dx2 (+ dy1 h) q h (+ q h) f nil))))

(defn- string-break
  [message]
  (let [words (str/split message #" ")]
    (loop [w 0
           l 0
           lines []
           text ""]
      (if (< w (count words))
        (recur
         (if (> (+ l (count (words w)) 1) constants/dialog-text-width)
           w
           (inc w))
         (if (> (+ l (count (words w)) 1) constants/dialog-text-width)
           0
           (+ 1 l (count (words w))))
         (if (> (+ l (count (words w)) 1) constants/dialog-text-width)
           (conj lines text)
           lines)
         (if (> (+ l (count (words w)) 1) constants/dialog-text-width)
           ""
           (str text (words w) " ")))
        (conj lines text)))))

(defn draw-cursor
  ([x y]
   (.drawImage @window/graphics @ui-pack x y (+ x 16) (+ y 16) 342 468 358 484 nil))
  ([x y line]
   (draw-cursor (+ x 10) (+ y 17 (* constants/line-spacing line)))))

(defn get-menu-cursor
  [state menu]
  (:cursor (util/filter-first #(= (.menu-type (:menu %)) menu) (:menus @state))))

(defn dialog
  [message]
  (let [y (- constants/screen-height constants/dialog-height)
        text (string-break message)]
    (draw-window 0 y constants/screen-width constants/dialog-height)
    (dorun
     (for [i (range (count text))]
       (draw-line 0 y i (get text i))))))

(defn draw-menus
  [menus]
  (dorun
   (for [m menus]
     (cond
       (:open m)
       (.draw (:menu m))))))

(defn open-menu!
  [state menu]
  (dosync
   (alter state update :menus conj {:menu menu
                                    :open true
                                    :cursor 0})))

(defn close-menu!
  [state]
  (dosync (alter state update-in [:menus] pop)))

(defn get-menu-index
  [state menu]
  (let [menus (:menus @state)]
    (loop [i 0]
      (if (= menu (.menu-type (:menu (menus i))))
        i
        (if (< i (dec (count menus)))
          (recur (inc i)))))))

(defn last-menu-index
  [state]
  (dec (count (:menus @state))))

(defn- cursor-can-move?
  [state]
  (> (.cursor-length (get-in @state [:menus (last-menu-index state) :menu])) 0))

(defn- change-cursor!
  [state f]
  (let [m (last-menu-index state)]
    (dosync (alter state update-in [:menus m :cursor] f))
    (if (= (.cursor-length (get-in @state [:menus m :menu])) (get-in @state [:menus m :cursor]))
      (dosync (alter state assoc-in [:menus m :cursor] 0))
      (if (< (get-in @state [:menus m :cursor]) 0)
        (dosync (alter state assoc-in [:menus m :cursor] (dec (.cursor-length (get-in @state [:menus m :menu])))))))))

(defn- dec-cursor!
  [state]
  (change-cursor! state dec))

(defn- inc-cursor!
  [state]
  (change-cursor! state inc))

(defn- above-min-quantity?
  [state]
  (let [{:keys [quantity-min quantity]} @state]
    (if (and quantity-min quantity)
      (< quantity-min quantity))))

(defn- below-max-quantity?
  [state]
  (let [{:keys [quantity-max quantity]} @state]
    (if (and quantity-max quantity)
      (< quantity quantity-max))))

(defn- inc-quantity!
  [state]
  (dosync (alter state update :quantity inc)))

(defn- dec-quantity!
  [state]
  (dosync (alter state update :quantity dec)))

(defn move-cursor!
  [state key]
  (cond
    (and
     (= key :up)
     (cursor-can-move? state))
    (dec-cursor! state)
    (and
     (= key :down)
     (cursor-can-move? state))
    (inc-cursor! state)
    (and
     (= key :left)
     (above-min-quantity? state))
    (dec-quantity! state)
    (and
     (= key :right)
     (below-max-quantity? state))
    (inc-quantity! state)))

(defn is-menu-open?
  [state]
  (> (count (:menus @state)) 0))

(defn text-fixed-width
  [text spaces]
  (let [text-str (str text)]
    (if (> (count text-str) spaces)
      (str (subs text-str 0 (- spaces 4)) "... ")
      (loop [t text-str]
        (if (> spaces (count t))
          (recur (str t " "))
          t)))))

(defn reset-quantity!
  [state min max]
  (dosync
   (alter state assoc :quantity 1 :quantity-min min :quantity-max max)))

(defn scrollable-area
  [x y cursor max-lines-on-screen start-line lines]
  (let [offset (max 0 (- cursor max-lines-on-screen))
        line-count (count lines)]
    (draw-cursor x y (- (inc cursor) offset))
    (loop [i 0]
      (if (< i line-count)
        (let [line (get lines i)
              line-number (+ start-line i)]
          (if (< offset line-number)
            (line (- line-number offset)))
          (recur (inc i)))))))

(defn draw-portraits
  ([state item selected-mob]
   (loop [i 0]
     (let [portrait-x 50
           portrait-y (-> (* 10 i)
                          (+ (* (second constants/portrait-size) i))
                          (+ (* constants/padding i)))
           {:keys [affect amount]} item
           {{:keys [party] {{:keys [hp max-hp mana max-mana portrait name]} i} :party} :player} @state
           amount-hp (if (= :restore-hp affect) (util/restore-amount amount hp max-hp))
           amount-mana (if (= :restore-mana affect) (util/restore-amount amount mana max-mana))]
       (.drawImage @window/graphics (:image portrait) constants/padding (+ 20 portrait-y) nil)
       (draw-line portrait-x portrait-y 0 name)
       (draw-line portrait-x
                  portrait-y
                  1
                  (str (format "%d/%d HP" hp max-hp)
                       (if (and (= i selected-mob)
                                (= :restore-hp affect))
                         (format " +%d" amount-hp))))
       (draw-line
        portrait-x
        portrait-y
        2
        (str (format "%d/%d Mana" mana max-mana)
             (if (and (= i selected-mob)
                      (= :restore-mana affect))
               (format " +%d" amount-mana))))
       (if (< i (dec (count party)))
         (recur (inc i))))))
  ([state]
   (draw-portraits state nil -1)))
