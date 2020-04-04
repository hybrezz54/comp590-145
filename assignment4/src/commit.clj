(ns commit
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn main
  "create a commit and advance the current branch"
  [dir db n]
  (let [flag (first n)
        branch (second n)
        db-path (str dir "/" db)
        head-path (str db-path "/" "HEAD")
        ref-path (str db-path "/refs/heads/" (if (= flag "-d") branch flag))]
    (try
      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot commit: create a commit and advance the current branch")
                (println)
                (println "Usage: idiot commit <tree> -m \" message \" [(-p parent)...]")
                (println)
                (println "Arguments:")
                (println "   -h               print this message")
                (println "   <tree>           the address of the tree object to commit")
                (println "   -m \" <message> \"   the commit message")
                (println "   -p <parent>      the address of a parent commit"))
            (and (= flag "-d") (nil? branch)) (throw (Exception.))
            (or (and (not= flag "-d") (> (count n) 1)) (and (= flag "-d") (> (count n) 2))) (println "Error: invalid arguments.")
            (not (.exists (io/file db-path))) (println "Error: could not find database. (Did you run `idiot init`?)")
            (and (= flag "-d") (not (.exists (io/file ref-path)))) (println (format "Error: branch '%s' not found." branch))
            (and (= flag "-d") (str/ends-with? (slurp head-path) branch)) (println (format "Error: cannot delete checked-out branch '%s'." branch))
            (= flag "-d") (do (io/delete-file ref-path)
                              (println (format "Deleted branch %s." branch)))
            :else (do (println (format "ref: refs/heads/%s\n" flag))
                      (println (format "Switched to branch '%s'" flag))))

      (catch Exception e
        e (println "Error: you must specify a branch name.")))))