(ns cat_file
  (:require [clojure.java.io :as io])
  (:require [utils]))

(defn handle-t
  "read contents of objects to determine their type"
  [obj-path]
  (let [obj (slurp (utils/unzip obj-path))
        type-end (.indexOf obj " ")]
    (subs obj 0 type-end)))

(defn read-blob
  "read the contents of a blob object"
  [obj-path]
  (let [blob (slurp (utils/unzip obj-path))
        blob-start (.indexOf blob "\000")]
    (subs blob (+ blob-start 1))))

(defn main
  "print information about an object"
  [dir db n]
  (let [flag (first n)
        addr (last n)
        db-path (str dir "/" db)
        obj-path (str db-path "/objects/" (subs addr 0 2) "/" (subs addr 2))]
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
            (not (or (= flag "-p") (= flag "-t"))) (println "Error: the -p or -t switch is required")
            (and (or (= flag "-p") (= flag "-t")) (= flag addr)) (throw (Exception.))
            (not (.exists (io/file obj-path))) (println "Error: that address doesn't exist")
            (= flag "-t") (println (handle-t obj-path))
            :else (let [type (handle-t obj-path)]
                    (cond (= type "blob") (print (read-blob obj-path))
                          (= type "tree") (println (str "read tree " addr))
                          :else (println (str "read commit " addr)))))

      (catch Exception e
        (println e) (println "Error: you must specify an address")))))