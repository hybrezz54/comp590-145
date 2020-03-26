(ns write_wtree
  (:require [clojure.java.io :as io])
  (:require [utils]))

(defn blob-addr
  "creates the blob address using sha-bytes"
  [file]
  (let [contents (slurp file)]
    (utils/sha-bytes (.getBytes (str "blob " (count contents) "\000" contents)))))

(defn blob-entry-bytes [file]
  (.getBytes (str "100644 " (.getName file) "\000")))

(defn entries? [files]
  (reduce + 0 (map count files)))

(defn create-object [dir db addr contents]
  (let [path (utils/obj-path dir db addr)]
    (cond (not (.exists (io/file path))) (do (io/make-parents path)
                                             (io/copy (utils/zip-str contents)
                                                      (io/file path))))))

(defn create-blob [dir db file]
  (let [header-plus-blob (str "blob " (count (slurp file)) "\000" (slurp file))
        addr (utils/sha1-sum header-plus-blob)]
    (create-object dir db addr header-plus-blob)))

(defn enter-blob [dir db file]
  (create-blob dir db file)
  (concat (blob-entry-bytes file) (blob-addr file)))

(defn enter-tree [name addr]
  (cond (nil? addr) nil
        :else (concat (.getBytes (str "40000 " name "\000")) (utils/from-hex-string addr))))

(defn create-tree [dir db contents]
  (let [filtered-contents (filter #(not (nil? %)) contents)
        entries (entries? filtered-contents)
        content-bytes (apply concat filtered-contents)
        tree-bytes (-> (str "tree " entries "\000") .getBytes (concat content-bytes) byte-array)
        addr (-> tree-bytes utils/sha-bytes utils/to-hex-string)]
    (cond (= (count filtered-contents) 0) (str "The directory was empty, so nothing was saved.")
          :else (do (create-object dir db addr tree-bytes)
                    addr))))

(defn build-tree [dir db current]
  (let [files (.listFiles current)
        sorted-files (sort-by #(.getName %) files)
        contents (for [file sorted-files]
                   (if (.isDirectory file)
                     (when (not= (.getName file) db)
                       (enter-tree (.getName file) (build-tree dir db file)))
                     (enter-blob dir db file)))]
    (create-tree dir db (vec contents))))

(defn main
  "write the working tree to the database"
  [dir db n]
  (let [flag (first n)
        db-path (str dir "/" db)]
    (try
      (if (and (not= flag nil) (not= flag "-h") (not= flag "--help"))
        (throw (Exception.)) ())

      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot write-wtree: write the working tree to the database")
                (println)
                (println "Usage: idiot write-wtree")
                (println)
                (println "Arguments:")
                (println "   -h       print this message"))
            (not (.exists (io/file db-path))) (println "Error: could not find database. (Did you run `idiot init`?)")
            :else (println (build-tree dir db (io/file dir))))

      (catch Exception e
        e (println "Error: write-wtree accepts no arguments")))))