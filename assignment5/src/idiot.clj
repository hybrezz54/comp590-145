(ns idiot
  (:require [clojure.java.io :as io])
  (:require [utils]
            [init]
            [help]
            [cat_file]
            [hash_object]
            [write_wtree]
            [commit_tree]
            [rev_parse]
            [switch]
            [branch]
            [commit]
            [rev_list]))

(defn execute
  [dir db args]
  (let [[cmd & tasks] args
        handle-r (fn [[rdir & rargs]]
                   (cond (< (count rargs) 1) (println "Error: the -r switch needs an argument")
                         (not (.exists (io/file rdir))) (println "Error: the directory specified by -r does not exist")
                         :else (execute rdir db rargs)))
        handle-d (fn [[ddb & dargs]]
                   (cond (< (count dargs) 1) (println "Error: the -d switch needs an argument")
                         :else (execute dir ddb dargs)))]
    (cond (= cmd "-r") (handle-r tasks)
          (= cmd "-d") (handle-d tasks)
          (or (= cmd "help") (= cmd "-h") (= cmd "--help")) (help/main tasks)
          (and (= cmd "init") (< (count tasks) 2)) (init/main dir db tasks)
          (and (= cmd "hash-object") (< (count tasks) 3)) (hash_object/main dir db tasks)
          (and (= cmd "cat-file") (< (count tasks) 3)) (cat_file/main dir db tasks)
          (and (= cmd "write-wtree") (< (count tasks) 2)) (write_wtree/main dir db tasks)
          (= cmd "commit-tree") (commit_tree/main dir db tasks)
          (= cmd "rev-parse") (rev_parse/main dir db tasks)
          (= cmd "switch") (switch/main dir db tasks)
          (= cmd "branch") (branch/main dir db tasks)
          (= cmd "commit") (commit/main dir db tasks)
          (= cmd "rev-list") (rev_list/main dir db tasks)
          :else (println "Error: invalid command"))))

(defn -main [& args]
  (cond (= (count args) 0) (help/main nil)
        :else (execute "./" ".idiot" args)))