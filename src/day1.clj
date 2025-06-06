(ns day1
  (:require [clojure.string :as s]))

(def file "input/day1.txt")

;; PART 1

;; https://clojuredocs.org/clojure.core/slurp
(def input (slurp file))
;; => "3   4\n4   3\n2   5\n1   3\n3   9\n3   3\n"

;; https://clojuredocs.org/clojure.string/split-lines
(def lines (s/split-lines input))
;; => ["3   4" "4   3" "2   5" "1   3" "3   9" "3   3"]

;; `#"..."` creates a regex expression.
;; https://clojure.org/guides/weird_characters#_regular_expression
;; `#(...)` creates an anonymous function and % is where the argument goes.
;; https://clojure.org/guides/weird_characters#_anonymous_function
;; https://clojuredocs.org/clojure.core/map
(def split-lines (map #(s/split % #"\s+") lines))
;; => (["3" "4"] ["4" "3"] ["2" "5"] ["1" "3"] ["3" "9"] ["3" "3"])

;; `apply` passes the elements of a collection as args to a function
;; The following is a convenient way to transpose a matrix.
;; https://clojuredocs.org/clojure.core/apply
(def transposed (apply map list split-lines))
;; => (("3" "4" "2" "1" "3" "3") ("4" "3" "5" "3" "9" "3"))

;; `partial` creates a curried function
;; https://clojuredocs.org/clojure.core/partial
;; https://clojuredocs.org/clojure.core/parse-long
(def list-of-ints (map (partial map parse-long) transposed))
;; => ((3 4 2 1 3 3) (4 3 5 3 9 3))

;; https://clojuredocs.org/clojure.core/sort
(def sorted (map sort list-of-ints))
;; => ((1 2 3 3 3 4) (3 3 3 4 5 9))

;; `%1` and `%2` represent the first and second arguments of the
;; anonymous function in #(...) respectively
;; https://clojure.org/guides/higher_order_functions#_function_literals
(def distances (apply map #(abs (- %1 %2)) sorted))
;; => (2 1 0 1 2 5)

;; Add them up
;; https://clojuredocs.org/clojure.core/reduce
(reduce + distances) ;; => 11

;; ALTERNATIVELY
;; Using `let` to create scoped bindings. Lesser pollution to global bindings.
;; https://clojuredocs.org/clojure.core/let
(let [input (slurp file)
      lines (s/split-lines input)
      split-lines (map #(s/split % #"\s+") lines)
      transposed (apply map list split-lines)
      ;; compose functions using `comp`
      ;; https://clojuredocs.org/clojure.core/comp
      sorted (map (comp sort
                        (partial map parse-long))
                  transposed)
      distances (apply map #(abs (- %1 %2)) sorted)
      answer (reduce + distances)]
  answer)

;; ALTERNATIVELY
;; In a thread-last chain
;; The `->>` is known as thread-last. It threads a result into the last argument of the next form for each form.
;; https://clojuredocs.org/clojure.core/-%3E%3E
(->> (slurp file)
     s/split-lines
     (map #(s/split % #"\s+"))
     (apply map list)
     (map (comp sort
                (partial map parse-long)))
     (apply map #(abs (- %1 %2)))
     (reduce +)) ;; => 11

;; PART 2

(let [;; Destructure the result to 2 variable bindings
      ;; https://clojure.org/guides/destructuring
      [left-list right-list] (->> (slurp file)
                                  s/split-lines
                                  (map #(s/split % #"\s+"))
                                  (apply map list)
                                  (map (partial map parse-long)))
      ;; => [(3 4 2 1 3 3) (4 3 5 3 9 3)]
      ;; https://clojuredocs.org/clojure.core/frequencies
      freq (frequencies right-list)
      ;; => {4 1, 3 3, 5 1, 9 1}
      ]
  (transduce
   (map #(or (some-> (freq %) (* %))
             0))
   +
   left-list)
   ;; => 31

  ;; `(freq %)` evaluates to the value of the key `%` in the hashmap `freq`, evaluating to `nil` if the key is absent.

  ;; The `some->` macro will thread a result into the first argument of the next form as long as the result is not nil, otherwise evaluates to nil.
  ;; https://clojuredocs.org/clojure.core/some-%3E

  ;; `or` evaluates to the first expression that is truthy (anything that isn't `nil` nor `false`) or the last expression.
  ;; https://clojuredocs.org/clojure.core/or

  ;; Transduce is a generalization of a transformation followed by a reduce.
  ;; In general, it is more efficient than doing the transformation followed by the reduce separately.
  ;; That's because `transduce` doesn't make intermittent lazy sequences.
  ;; https://clojuredocs.org/clojure.core/transduce

  ;; Check out Rich Hickey's talks on transducers to understand how they work internally.

  ;; In our case, it's a map followed by a reduce

  ;; NOTE: #_(...) comments out the sexp (not evaluated)
  ;; https://clojure.org/guides/weird_characters#_discard

  ;; Equivalent to but more efficient than
  #_(->> left-list
         (map #(or (some-> (freq %) (* %))
                   0))
         (reduce +)))
