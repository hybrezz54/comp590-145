(ns branch
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn current-branch
  "get the name of the current branch"
  [dir db]
  (let [db-path (str dir "/" db)
        head-path (str db-path "/" "HEAD")]
    (-> head-path slurp (str/split #"\n") first (str/split #"/") last)))

(defn list-branches
  "list the names of all branches"
  [dir db]
  (let [db-path (str dir "/" db)
        branches (->> db-path io/file .listFiles (sort-by #(.getName %)))
        current (current-branch db dir)]
    (doseq [branch branches] (let [name (.getName branch)]
                               (if (not= name current) (println (str "  " name))
                                   (println (str "* " name)))))))

(defn main
  "list or delete branches"
  [dir db n]
  (let [flag (first n)
        branch (second n)
        db-path (str dir "/" db)
        ref-path (str db-path "/refs/heads/" (if (= flag "-d") branch flag))]
    (try
      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot branch: list or delete branches")
                (println)
                (println "Usage: idiot branch [-d <branch>]")
                (println)
                (println "Arguments:")
                (println "   -d <branch>   delete branch <branch>"))
            (and (= flag "-d") (nil? branch)) (throw (Exception.))
            (or (and (not= flag "-d") (not (nil? flag))) (< 2 (count n))) (println "Error: invalid arguments.")
            (not (.exists (io/file db-path))) (println "Error: could not find database. (Did you run `idiot init`?)")
            (and (= flag "-d") (not (.exists (io/file ref-path)))) (println (format "Error: branch '%s' not found." branch))
            (= flag "-d") (cond (= (current-branch dir db) branch) (println (format "Error: cannot delete checked-out branch '%s'." branch))
                                :else (do (io/delete-file ref-path)
                                          (println (format "Deleted branch %s." branch))))
            :else (list-branches dir db))

      (catch Exception e
        e (println "Error: you must specify a branch name.")))))