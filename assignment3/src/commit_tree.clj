(ns commit_tree
  (:require [clojure.java.io :as io]))

(defn main
  "write a commit object based on the given tree"
  [dir db n]
  (let [flag (first n)]
    (try
      (if (and (not= flag nil) (not= flag "-h") (not= flag "--help"))
        (throw (Exception.)) ())

      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot commit-tree: write a commit object based on the given tree")
                (println)
                (println "Usage: idiot commit-tree <tree> -m \"message\" [(-p parent)...]")
                (println)
                (println "Arguments:")
                (println "   -h       print this message"))
            (not (.exists (io/file ".git"))) (println "Error: could not find database. (Did you run `idiot init`?)")
            :else (println "Do something"))

      (catch Exception e
        e (println "Error: commit-tree accepts arguments")))))