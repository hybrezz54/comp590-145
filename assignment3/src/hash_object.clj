(ns hash_object
  (:require [clojure.java.io :as io])
  (:require [utils]))

(defn main
  "compute address and maybe create blob from file"
  [dir db n]
  (let [flag (first n)
        file (str dir "/" (last n))
        db-path (str dir "/" db)]
    (try
      (if (and (not= flag "-h") (not= flag "--help") (not= flag "-w") (= (first flag) "-"))
        (throw (Exception.)) ())
      ; (println (.exists (io/file file)))

      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot hash-object: compute address and maybe create blob from file")
                (println)
                (println "Usage: idiot hash-object [-w] <file>")
                (println)
                (println "Arguments:")
                (println "   -h       print this message")
                (println "   -w       write the file to database as a blob object")
                (println "   <file>   the file"))
            (not (.exists (io/file db-path))) (println "Error: could not find database. (Did you run `idiot init`?)")
            (and (= flag "-w") (= flag file)) (throw (Exception.))
            (.exists (io/file file)) (let [header-plus-blob (str "blob " (count (slurp file)) "\000" (slurp file))
                                           address (utils/sha1-sum header-plus-blob)
                                           blob-dir (subs address 0 2)
                                           blob-file (subs address 2)]
                                       (cond (= flag "-w")
                                             (do (.mkdirs (io/as-file (str db-path "/objects/" blob-dir)))
                                                 (io/copy (utils/zip-str header-plus-blob)
                                                          (io/file (str db-path "/objects/" blob-dir "/" blob-file)))))
                                       (println address))
            :else (println "Error: that file isn't readable"))

      (catch Exception e
        e (println "Error: you must specify a file.")))))