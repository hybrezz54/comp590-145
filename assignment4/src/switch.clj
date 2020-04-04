(ns switch
  (:require [clojure.java.io :as io])
  (:require [rev_parse :as rp]))

(defn main
  "change what HEAD points to"
  [dir db n]
  (let [flag (first n)
        branch (second n)
        db-path (str dir "/" db)
        head-path (str db-path "/" "HEAD")
        ref-path (str db-path "/refs/heads/" (if (= flag "-c") branch flag))]
    (try
      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot switch: change what HEAD points to")
                (println)
                (println "Usage: idiot switch [-c] <branch>")
                (println)
                (println "Arguments:")
                (println "   -c   create the branch before switching to it"))
            (nil? flag) (throw (Exception.))
            (or (and (not= flag "-c") (> (count n) 1)) (and (= flag "-c") (> (count n) 2))) (println "Error: you may only specify one branch name.")
            (not (.exists (io/file db-path))) (println "Error: could not find database. (Did you run `idiot init`?)")
            (and (= flag "-c") (.exists (io/file ref-path))) (println "Error: a ref with that name already exists.")
            (not (.exists (io/file ref-path))) (println "Error: no ref with that name exists.")
            (= flag "-c") (do (spit ref-path (rp/find-commit db dir))
                              (spit head-path (format "ref: refs/heads/%s\n" branch))
                              (println (format "Switched to a new branch '%s'" branch)))
            :else (do (spit head-path (format "ref: refs/heads/%s\n" flag))
                      (println (format "Switched to branch '%s'" flag))))

      (catch Exception e
        e (println "Error: you must specify a branch name.")))))