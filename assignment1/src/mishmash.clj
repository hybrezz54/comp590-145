(ns mishmash
  (:require [clojure.string :as str]))

; define vectors and hashmaps relevant to reading and outputting roman numerals
(def M ["" "M" "MM" "MMM"])
(def C ["" "C" "CC" "CCC" "CD" "D" "DC" "DCC" "DCCC" "CM"])
(def X ["" "X" "XX" "XXX" "XL" "L" "LX" "LXX" "LXXX" "XC"])
(def I ["" "I" "II" "III" "IV" "V" "VI" "VII" "VIII" "IX"])
(def roman-vals {"M" 1000, "D" 500, "C" 100, "L" 50, "X" 10, "V" 5, "I" 1})
(def roomi-map {\M 1000, \D 500, \C 100, \L 50, \X 10, \V 5, \I 1})

(defn factorial
  "Compute the factorial of a number"
  [n]
  ; (loop [current n
  ;        next (dec current)
  ;        total 1]
  ;   (if (> current 1)
  ;     (recur next (dec next) (* total current))
  ;     total)))
  (reduce * (range 1 (inc n))))

(defn binomial
  "Calculate the binomial coefficient."
  [n k]
  (/ (factorial n)
     (* (factorial k)
        (factorial
         (- n k)))))

(defn roman-converter
  "Helper used for reading function"
  [x y]
  (if (> x (* 4 y))
    (- x y)
    (+ y x)))

(defn pascal
  "Prints the corresponding row of Pascal's triangle"
  [x]
  (try
    (let [n (Integer/parseInt x)]
      (if (< n 0) (throw (NumberFormatException.)) ())
      (def b [])
      (dotimes [i (inc n)]
        (def b (conj b (binomial n i))))
      (println (str/join " " b)))
    (catch NumberFormatException e
      e (println "invalid input"))))

(defn write-roman
  "Accepts a number and prints the corresponding roman numeral"
  [x]
  (try
    (let [n (Integer/parseInt x)]
      (cond (or (< n 1) (>= n 4000)) (throw (NumberFormatException.))
            :else (let [thp (nth M (quot n 1000))
                        hp (nth C (quot (mod n 1000) 100))
                        tp (nth X (quot (mod n 100) 10))
                        op (nth I (mod n 10))]
                    (println (str thp hp tp op)))))
    (catch NumberFormatException e
      e (println "invalid input"))))

(defn read-roman
  "Accepts a roman numeral and prints the corresponding decimal number"
  [x]
  ; (def flip (reverse (str x)))
  ; (def finder (map roomi-map flip))
  ; (def q [])
  (try
    (def isBlank (str/blank? x))
    (def sum 0)
    (def check true)
    (def romMap {\I 1, \V 5, \X 10, \L 50, \C 100, \D 500, \M 1000})

    (doseq [i x]
      (if (not (contains? romMap i)) (def check false) ()))

    (def conditions (and (not (= (str x) "IIII")) (not isBlank) (= check true)))

    (cond
      (and (not (= (str x) "IIII")) (not (= (str x) "IXCM")) (not (= (str x) "IC")) (not (= (str x) "XD"))
           (not (= (str x) "ID")) (not (= (str x) "IIXC")) (not (= (str x) "IL"))
           (not (= (str x) "IM")) (not (= (str x) "XM")) (not isBlank) (= check true))
      (def sum (reduce roman-converter (map romMap (reverse x))))
      :else (def sum "invalid input"))
    (def valList (vals romMap))
    (doseq [v valList]
      (if (and (> (count x) 1) (= sum v)) (def sum "invalid input"))
      (if (and (not (and (> (count x) 1) (= sum v))) conditions) (def sum sum)))
    (println sum)
  ;   (if (contains? roomi-map (str x)) (println (reduce roman-converter finder)) ())
    ; (doseq [c x]
    ;   (if (not (contains? roman-vals (str c))) 
    ;     (throw (AssertionError.)) ())
    ;   (def q (conj q (str c)))
    ;   (let [j (str/join q)]
    ;     ; (println j q)
    ;     (cond (or (contains? M j) (contains? C j) (contains? X j) (contains? I j)) (println j)
    ;           (not (contains? M j)) (do (print (.indexOf M (str/join (pop q)))) (def q []))
    ;           (not (contains? C j)) (do (print (.indexOf C (str/join (pop q)))) (def q []))
    ;           (not (contains? X j)) (do (print (.indexOf X (str/join (pop q)))) (def q []))
    ;           (not (contains? I j)) (do (print (.indexOf I (str/join (pop q)))) (def q []))
    ;           :else ())))
    (catch AssertionError e
      e (println "invalid input"))))


(defn -main [& args]
  (cond (and (= (first args) "pascal") (= (count (rest args)) 1)) (pascal (first (rest args)))
        (and (= (first args) "write-roman") (= (count (rest args)) 1)) (write-roman (first (rest args)))
        (and (= (first args) "read-roman") (= (count (rest args)) 1)) (read-roman (first (rest args)))
        :else (println "invalid input")))