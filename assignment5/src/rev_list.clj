(ns rev_list
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as str]))

(defn main
  "list preceding revisions, latest first"
  [dir db n]
  (let [flag (first n)
        db-path (str dir "/" db)
        ref-path (str db-path "/refs/heads/" flag)]
    (try
      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot rev-list: list preceding revisions, latest first")
                (println)
                (println "Usage: idiot rev-list [-n <count>] [<ref>]")
                (println)
                (println "Arguments:")
                (println "   -n <count>   stop after <count> revisions (default: don't stop)")
                (println "   <ref>        a reference; see the rev-parse command (default: HEAD)"))
            (nil? flag) (throw (Exception.))
            (> (count n) 1) (println "Error: you must specify a branch name and nothing else.")
            (not (.exists (io/file db-path))) (println "Error: could not find database. (Did you run `idiot init`?)")
            (not (.exists (io/file ref-path))) (println (format "Error: could not find ref named %s." flag))
            :else (print (slurp ref-path)))

      (catch Exception e
        e (println "Error: you must specify a numeric count with '-n'.")))))