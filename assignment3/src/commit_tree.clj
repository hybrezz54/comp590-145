(ns commit_tree
  (:require [clojure.java.io :as io])
  (:require [utils]
            [cat_file :as cf]))

(def author "Linus Torvalds <torvalds@transmeta.com> 1581997446 -0500")
(def committer author)

(defn create-object [dir db addr contents]
  (let [path (utils/obj-path dir db addr)]
    (cond (not (.exists (io/file path))) (do (io/make-parents path)
                                             (io/copy (utils/zip-str contents)
                                                      (io/file path))))))

(defn commit-object
  [tree-addr author-str committer-str msg parent-str]
  (let [tree-str (utils/to-hex-string tree-addr)
        commit-format (str "tree %s\n"
                           "%s"
                           "author %s\n"
                           "committer %s\n"
                           "\n"
                           "%s\n")
        commit-str (format commit-format
                           tree-str
                           parent-str
                           author-str
                           committer-str
                           msg)]
    (format "commit %d\000%s"
            (count commit-str)
            commit-str)))

(defn check-parents
  "Check if valid parent commit addresses are given"
  [dir db addrs]
  (loop [addr-seq (seq addrs)]
    (if addr-seq
      (let [addr (first addr-seq)]
        (cond (not (.exists (io/file (utils/obj-path dir db addr)))) (println (str "Error: no commit object exists at address " addr "."))
              (not= (cf/get-type dir db addr) "commit") (println (str "Error: an object exists at address " addr ", but it isn't a commit."))
              :else (recur (next addr-seq))))
      ())))

(defn main
  "write a commit object based on the given tree"
  [dir db n]
  (let [[addr msg-flag msg par-flag & par-addrs] n
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
            (and (= par-flag "-p") (nil? par-addrs)) (println "Error: you must specify a commit object with the -p switch.")
            :else (do (if (not (nil? par-addrs))
                        (check-parents dir db par-addrs) ())
                      (print "")))

      (catch Exception e
        e (println "Error: you must specify a tree address.")))))