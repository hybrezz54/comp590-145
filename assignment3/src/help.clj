(ns help)

(def cmd-map {"help" "help/main"
               "init" "init/main"
               "hash-object" "hash_object/main"
               "cat-file" "cat_file/main"
               "write-wtree" "write_wtree/main"
               "commit_tree" "commit_tree/main"})

(defn main
  "Print command usage info"
  [dir db n]
  (let [flag (first n)]
    (try
      (if (and (not= flag nil) (not= flag "-h") (not= flag "--help") (not= flag "help")
               (not= flag "init") (not= flag "hash-object") (not= flag "cat-file")
               (not= flag "write-wtree") (not= flag "commit-tree"))
        (throw (Exception.)) ())

      (if (or (= flag "help") (= flag "init") (= flag "hash-object") (= flag "cat-file")
              (= flag "write-wtree") (= flag "commit-tree"))
        ((load-string (get cmd-map flag)) "" "" ["-h"])
        (do (if (or (= flag "-h") (= flag "--help"))
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
            (println "   help")
            (println "   init")
            (println "   hash-object [-w] <file>")
            (println "   cat-file {-p|-t} <address>")
            (println "   write-wtree")
            (println "   commit-tree <tree> -m \"<message>\" [(-p <parent>)...]")))
      (catch Exception e
        e (println "Error: invalid command")))))