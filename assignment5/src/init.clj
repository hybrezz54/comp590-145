(ns init
  (:require [clojure.java.io :as io]))

(defn main
  "initialize a new database"
  [dir db n]
  (let [flag (first n)
        db-path (str dir "/" db)
        objs-path (str db-path "/objects")
        refs-path (str db-path "/refs/heads")
        head-path (str db-path "/HEAD")]
    (try
      (if (and (not= flag nil) (not= flag "-h") (not= flag "--help"))
        (throw (Exception.)) ())

      (if (or (= flag "-h") (= flag "--help"))
        (do (println "idiot init: initialize a new database")
            (println)
            (println "Usage: idiot init")
            (println)
            (println "Arguments:")
            (println "   -h   print this message"))
        (if (.exists (io/file db-path))
          (println (str "Error: " db " directory already exists"))
          (do (.mkdirs (io/file objs-path))
              (.mkdirs (io/file refs-path))
              (spit head-path "ref: refs/heads/master\n")
              (println (str "Initialized empty Idiot repository in " db " directory")))))

      (catch Exception e
        e (println "Error: init accepts no arguments")))))