(ns cat_file
  (:require [clojure.java.io :as io])
  (:require [utils]))

(defn main
  "print information about an object"
  [dir db n]
  (let [flag (first n)
        addr (last n)
        db-path (str dir "/" db)]
    (try
      (if (and (not= flag "-h") (not= flag "--help") (not= flag "-p") (not= flag "-t") (= (first flag) "-"))
        (throw (Exception.)) ())

      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot cat-file: print information about an object")
                (println)
                (println "Usage: idiot cat-file {-p|-t} <address>")
                (println)
                (println "Arguments:")
                (println "   -h          print this message")
                (println "   -p          pretty-print contents based on object type")
                (println "   -t          print the type of the given object")
                (println "   <address>   the SHA1-based address of the object"))
            (not (.exists (io/file db-path))) (println "Error: could not find database. (Did you run `idiot init`?)")
            (not (= flag "-p")) (println "Error: the -p switch is required")
            (and (= flag "-p") (= flag addr)) (throw (Exception.))
            :else (let [blob-dir (subs addr 0 2)
                        blob-file (subs addr 2)]
                    (if (.exists (io/file (str db-path "/objects/" blob-dir "/" blob-file)))
                      (let [blob (with-open [input (-> (str db-path "/objects/" blob-dir "/" blob-file) io/file io/input-stream)] (utils/unzip input))
                            blob-start (.indexOf blob "\000")]
                        (print (subs blob (+ blob-start 1))))
                      (println "Error: that address doesn't exist"))))

      (catch Exception e
        e (println "Error: you must specify an address")))))