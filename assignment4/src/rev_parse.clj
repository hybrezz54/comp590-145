(ns rev_parse
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as str]))

(defn find-commit
  "Find the commit address that HEAD points to"
  [dir db]
  (let [db-path (str dir "/" db)
        head-path (str db-path "/" "HEAD")
        head (slurp head-path)]
    (if (str/starts-with? head "ref:")
      (let [ref (-> head (str/split #" ") second str/trim-newline)]
        (slurp (str db-path "/" ref))) ;; points to ref and assume ref is valid
      head))) ;; commit object

(defn main
  "determine which commit a ref points to"
  [dir db n]
  (let [flag (first n)
        db-path (str dir "/" db)
        ref-path (str db-path "/refs/heads/" flag)]
    (try
      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot rev-parse: determine which commit a ref points to")
                (println)
                (println "Usage: idiot rev-parse <ref>")
                (println)
                (println "<ref> can be:")
                (println "- a branch name, like 'master'")
                (println "- literally 'HEAD'")
                (println "- literally '@', an alias for 'HEAD'"))
            (nil? flag) (throw (Exception.))
            (> (count n) 1) (println "Error: you must specify a branch name and nothing else.")
            (not (.exists (io/file db-path))) (println "Error: could not find database. (Did you run `idiot init`?)")
            (or (= flag "HEAD") (= flag "@")) (print (find-commit dir db))
            (not (.exists (io/file ref-path))) (println (format "Error: could not find ref named %s." flag))
            :else (print (slurp ref-path)))

      (catch Exception e
        e (println "Error: you must specify a branch name.")))))