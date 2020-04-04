(ns commit_utils
  (:require [clojure.java.io :as io])
  (:require [utils]
            [cat_file :as cf]))

(defn commit-object
  "Construct a new commit object"
  [tree-addr author-str committer-str msg parent-str]
  (let [commit-format (str "tree %s\n"
                           "%s"
                           "author %s\n"
                           "committer %s\n"
                           "\n"
                           "%s\n")
        commit-str (format commit-format
                           tree-addr
                           parent-str
                           author-str
                           committer-str
                           msg)]
    (format "commit %d\000%s"
            (count commit-str)
            commit-str)))

(defn create-commit
  "Create and store a commit object in the database"
  [dir db commit]
  (let [commit-bytes (-> commit .getBytes utils/sha-bytes)
        addr (-> commit-bytes utils/to-hex-string)]
    (utils/create-object dir db addr commit)
    addr))

(defn check-parents
  "Check if valid parent commit addresses are given"
  [dir db parents]
  (loop [parents-seq (seq parents)]
    (if parents-seq
      (let [pair (first parents-seq)
            flag (first pair)
            addr (second pair)]
        (if (= flag "-p")
          (cond (not (.exists (io/file (utils/obj-path dir db addr)))) (str "Error: no commit object exists at address " addr ".")
                (not= (cf/get-type dir db addr) "commit") (str "Error: an object exists at address " addr ", but it isn't a commit.")
                :else (recur (next parents-seq)))
          (str "Error: invalid command")))
      nil)))