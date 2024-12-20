(ns cljrpgengine.class)

(def classes (assoc {}
                    :warrior
                    {:hp-gains [4 4]
                     :mana-gains [2 2]}

                    :mage
                    {:hp-gains [2 3]
                     :mana-gains [4 4]}

                    :rogue
                    {:hp-gains [3 4]
                     :mana-gains [2 3]}

                    :cleric
                    {:hp-gains [3 3]
                     :mana-gains [3 4]}

                    :unspecified
                    {:hp-gains [1 1]
                     :mana-gains [1 1]}))

(defn gain-for-level
  "Gains are represented as a vector of 2 numbers representing dice notation.
  The first number is the base, the second number is the modifier.  For
  example, [1 4] means 1d4, which means (+ 1 (rand-int 4))."
  [gains]
  (reduce (fn [a _]
            (+ a (inc (rand-int (second gains)))))
          0
          (range 0 (first gains))))

(defn hp-for-level
  [class]
  (gain-for-level (get-in classes [class :hp-gains])))

(defn mana-for-level
  [class]
  (gain-for-level (get-in classes [class :mana-gains])))
