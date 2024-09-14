(ns cljrpgengine.class)

(def classes (assoc {}
                    :warrior
                    {:hp-gains [15 8]
                     :mana-gains [5 3]}

                    :mage
                    {:hp-gains [5 4]
                     :mana-gains [15 12]}

                    :rogue
                    {:hp-gains [9 6]
                     :mana-gains [7 6]}

                    :cleric
                    {:hp-gains [8 5]
                     :mana-gains [12 10]}))

(defn gain-for-level
  "Gains are represented as a vector of 2 numbers representing dice notation.
  The first number is the base, the second number is the modifier.  For
  example, [1 4] means 1d4, which means (+ 1 (rand-int 4))."
  [gains]
  (-> (first gains)
      (+ (rand-int (second gains)))))

(defn hp-for-level
  [class]
  (gain-for-level (get-in classes [class :hp-gains])))

(defn mana-for-level
  [class]
  (gain-for-level (get-in classes [class :mana-gains])))
