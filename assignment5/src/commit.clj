(ns commit
  (:require [clojure.java.io :as io])
  (:require [utils]
            [commit_utils :as cu]
            [cat_file :as cf]
            [branch]
            [rev_parse :as rp]))

(def author "Linus Torvalds <torvalds@transmeta.com> 1581997446 -0500")
(def committer author)

(defn handle-commit
  "Update HEAD to point to new commit appropriately"
  [dir db addr]
  (println "Commit created.")
  (let [db-path (str dir "/" db)
        current (branch/current-branch dir db)
        head-path (str db-path "/" "HEAD")
        is-ref? (-> head-path slurp rp/is-head-ref?)
        ref-path (str db-path "/refs/heads/" current)]
    (when is-ref? (spit ref-path (str addr "\n"))
          (println (format "Updated branch %s." current)))))

(defn main
  "create a commit and advance the current branch"
  [dir db n]
  (let [[addr msg-flag msg & parents] n
        addr-flag-pairs (partition 2 parents)
        addrs (map second addr-flag-pairs)
        db-path (str dir "/" db)]
    (try
      (cond (or (= addr "-h") (= addr "--help"))
            (do (println "idiot commit: create a commit and advance the current branch")
                (println)
                (println "Usage: idiot commit <tree> -m \"message\" [(-p parent)...]")
                (println)
                (println "Arguments:")
                (println "   -h               print this message")
                (println "   <tree>           the address of the tree object to commit")
                (println "   -m \"<message>\"   the commit message")
                (println "   -p <parent>      the address of a parent commit"))
            (not (.exists (io/file db-path))) (println "Error: could not find database. (Did you run `idiot init`?)")
            (or (nil? addr) (= msg-flag addr)) (throw (Exception.))
            (not (.exists (io/file (utils/obj-path dir db addr)))) (println "Error: no tree object exists at that address.")
            (not= (cf/get-type dir db addr) "tree") (println "Error: an object exists at that address, but it isn't a tree.")
            (not= msg-flag "-m") (println "Error: you must specify a message.")
            (nil? msg) (println "Error: you must specify a message with the -m switch.")
            (odd? (count parents)) (println "Error: you must specify a commit object with the -p switch.")
            :else (if (not (nil? parents))
                    (let [results (cu/check-parents dir db addr-flag-pairs)]
                      (if results
                        (println results)
                        (let [commit-addr (->> addrs
                                               (map #(str "parent " % "\n"))
                                               (reduce str)
                                               (cu/commit-object addr author committer msg)
                                               (cu/create-commit dir db))]
                          (handle-commit dir db commit-addr))))
                    (let [commit-addr (->> (cu/commit-object addr author committer msg "")
                                           (cu/create-commit dir db))]
                      (handle-commit dir db commit-addr))))

      (catch Exception e
        e (println "Error: you must specify a tree address.")))))