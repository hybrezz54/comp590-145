(ns hash_object
  (:require [clojure.java.io :as io])
  (:require [utils]))

(defn main
  "compute address and maybe create blob from file"
  [dir db n]
  (let [flag (first n)
        file (last n)]
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
            (not (.exists (io/file ".git"))) (println "Error: could not find database. (Did you run `idiot init`?)")
            (and (= flag "-w") (= flag file)) (throw (Exception.))
            (.exists (io/file file)) (let [header-plus-blob (str "blob " (count (slurp file)) "\000" (slurp file))
                                           address (utils/sha1-sum header-plus-blob)
                                           dir (subs address 0 2)
                                           name (subs address 2)]
                                       (cond (= flag "-w")
                                             (do (.mkdirs (io/as-file (str ".git/objects/" dir)))
                                                 (io/copy (utils/zip-str header-plus-blob)
                                                          (io/file (str ".git/objects/" dir "/" name)))))
                                       (println address))
            :else (println "Error: that file isn't readable"))

      (catch Exception e
        e (println "Error: you must specify a file.")))))