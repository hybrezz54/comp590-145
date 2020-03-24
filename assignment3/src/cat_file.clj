(ns cat_file
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:require [utils]))

(defn handle-t
  "read contents of objects to determine their type"
  [dir db addr]
  (let [obj-path (utils/obj-path dir db addr)
        obj (slurp (utils/unzip obj-path))
        type-end (.indexOf obj " ")]
    (subs obj 0 type-end)))

(defn read-blob
  "read the contents of a blob object"
  [dir db addr]
  (let [obj-path (utils/obj-path dir db addr)
        blob (slurp (utils/unzip obj-path))]
    (second (string/split blob #"\000"))))

(defn read-tree-bytes
  "parse single line of a tree's content"
  [dir db content-bytes]
  (when (> (count content-bytes) 0)
    (let [addr (->> content-bytes (utils/split-at-byte 0) second (utils/split-at-byte 0) first (take 20) utils/to-hex-string)
          type-and-name (string/split (->> content-bytes (utils/split-at-byte 0) first utils/bytes->str) #" ")
          rest-bytes (->> content-bytes (utils/split-at-byte 0) second (drop 20) byte-array)]
      (str (first type-and-name) " " (handle-t dir db addr) " " addr "\t" (second type-and-name) "\n" (read-tree-bytes dir db rest-bytes)))))

(defn read-tree
  "read the contents of a tree object"
  [dir db addr]
  (let [obj-path (utils/obj-path dir db addr)
        content-bytes (->> obj-path utils/unzip (utils/split-at-byte 0) second)]
    (string/replace (->> content-bytes (read-tree-bytes dir db) string/trim) #"40000" "040000")))

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
            (not (or (= flag "-p") (= flag "-t"))) (println "Error: the -p or -t switch is required")
            (and (or (= flag "-p") (= flag "-t")) (= flag addr)) (throw (Exception.))
            (not (.exists (io/file (utils/obj-path dir db addr)))) (println "Error: that address doesn't exist")
            (= flag "-t") (println (handle-t dir db addr))
            :else (let [type (handle-t dir db addr)]
                    (cond (= type "blob") (print (read-blob dir db addr))
                          (= type "tree") (println (read-tree dir db addr))
                          :else (print (read-blob dir db addr)))))

      (catch Exception e
        e (println "Error: you must specify an address")))))