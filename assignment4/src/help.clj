(ns help)

(def cmd-map {"help" "help/main"
              "init" "init/main"
              "hash-object" "hash_object/main"
              "cat-file" "cat_file/main"
              "write-wtree" "write_wtree/main"
              "commit-tree" "commit_tree/main"
              "rev-parse" "rev_parse/main"
              "switch" "switch/main"
              "branch" "branch/main"
              "commit" "commit/main"})

(defn main
  "Print command usage info"
  [n]
  (let [flag (first n)]
    (try
      (if (and (not= flag nil) (not= flag "-h") (not= flag "--help") (not= flag "help")
               (not= flag "init") (not= flag "hash-object") (not= flag "cat-file")
               (not= flag "write-wtree") (not= flag "commit-tree") (not= flag "rev-parse")
               (not= flag "switch") (not= flag "branch") (not= flag "commit"))
        (throw (Exception.)) ())

      (if (or (= flag "init") (= flag "hash-object") (= flag "cat-file")
              (= flag "write-wtree") (= flag "commit-tree") (= flag "rev-parse")
              (= flag "switch") (= flag "branch") (= flag "commit"))
        ((load-string (get cmd-map flag)) "" "" ["-h"])
        (do (if (or (= flag "-h") (= flag "--help") (= flag "help"))
              (do (println "idiot help: print help for a command")
                  (println)
                  (println "Usage: idiot help <command>")
                  (println)
                  (println "Arguments:")
                  (println "   <command>   the command to print help for")
                  (println))
              (do (println "idiot: the other stupid content tracker")
                  (println)
                  (println "Usage: idiot [<top-args>] <command> [<args>]")
                  (println)
                  (println "Top-level arguments:")
                  (println "   -r <dir>   run from the given directory instead of the current one")
                  (println "   -d <dir>   store the database in <dir> (default: .idiot)")
                  (println)))

            (println "Commands:")
            (println "   branch [-d <branch>]")
            (println "   cat-file {-p|-t} <address>")
            (println "   commit <tree> -m \"message\" [(-p parent)...]")
            (println "   commit-tree <tree> -m \"message\" [(-p parent)...]")
            (println "   hash-object [-w] <file>")
            (println "   help")
            (println "   init")
            (println "   rev-parse <ref>")
            (println "   switch [-c] <branch>")
            (println "   write-wtree")))
      (catch Exception e
        e (println "Error: invalid command")))))