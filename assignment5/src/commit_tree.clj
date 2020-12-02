(ns commit_tree
  (:require [clojure.java.io :as io])
  (:require [utils]
            [commit_utils :as cu]
            [cat_file :as cf]))

(def author "Linus Torvalds <torvalds@transmeta.com> 1581997446 -0500")
(def committer author)

(defn main
  "write a commit object based on the given tree"
  [dir db n]
  (let [[addr msg-flag msg & parents] n
        addr-flag-pairs (partition 2 parents)
        addrs (map second addr-flag-pairs)
        db-path (str dir "/" db)]
    (try
      (cond (or (= addr "-h") (= addr "--help"))
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
                        (println (->> addrs
                                      (map #(str "parent " % "\n"))
                                      (reduce str)
                                      (cu/commit-object addr author committer msg)
                                      (cu/create-commit dir db)))))
                    (println (->> (cu/commit-object addr author committer msg "")
                                  (cu/create-commit dir db)))))

      (catch Exception e
        e (println "Error: you must specify a tree address.")))))