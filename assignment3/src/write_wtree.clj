(ns write_wtree
  (:require [clojure.java.io :as io])
  (:require [utils]))

(defn main
  "write the working tree to the database"
  [dir db n]
  (let [flag (first n)
        db-path (str dir "/" db)]
    (try
      (if (and (not= flag nil) (not= flag "-h") (not= flag "--help"))
        (throw (Exception.)) ())

      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot write-wtree: write the working tree to the database")
                (println)
                (println "Usage: idiot write-wtree")
                (println)
                (println "Arguments:")
                (println "   -h       print this message"))
            (not (.exists (io/file db-path))) (println "Error: could not find database. (Did you run `idiot init`?)")
            :else (println "Do something"))

      (catch Exception e
        e (println "Error: write-wtree accepts no arguments")))))