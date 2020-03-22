(ns commit_tree
  (:require [clojure.java.io :as io]))

(defn main
  "write a commit object based on the given tree"
  [dir db n]
  (let [flag (first n)
        db-path (str dir "/" db)]
    (try
      (if (and (not= flag nil) (not= flag "-h") (not= flag "--help"))
        (throw (Exception.)) ())

      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot commit-tree: write a commit object based on the given tree")
                (println)
                (println "Usage: idiot commit-tree <tree> -m \"message\" [(-p parent)...]")
                (println)
                (println "Arguments:")
                (println "   -h               print this message")
                (println "   <tree>           the address of the tree object to commit")
                (println "   -m \"<message>\"   the commit message")
                (println "   -p <parent>      the address of a parent commit"))
            (not (.exists (io/file db-path))) (println "Error: could not find database. (Did you run `idiot init`?)")
            :else (println "Do something"))

      (catch Exception e
        e (println "Error: commit-tree accepts arguments")))))