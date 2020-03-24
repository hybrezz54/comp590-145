(ns write_wtree
  (:require [clojure.java.io :as io])
  (:require [utils]))

(defn create-path [addr dir-db]
  (str dir-db "/objects/" (subs addr 0 2) "/" (subs addr 2)))

(defn addr-obj [o-bytes]
  (utils/string o-bytes))

(defn create-object [o-bytes dir db]
  (let [adlabel (addr-obj o-bytes)
        path (create-path adlabel (str dir db))]
    (cond (not (.exists (io/as-file path))) (do (io/make-parents path)
                                                (io/copy (utils/zip-str o-bytes) (io/file path)))) adlabel))

(defn blob-addr
  "creates the blob address using sha-bytes"
  [file]
  (str "blob " (count (slurp file)) "\000" (slurp file)))

(defn blob-addr-to-hex [file]
  (utils/from-hex-string (utils/sha1-sum (blob-addr file))))

(defn blob-bytes [file]
  (.getBytes (str "100644 " (.getName file) "\000")))


(defn enter-blob [dir db file]
  (create-object (subs (.getPath file) (count dir)) dir db)
  (utils/concat-bytes (blob-bytes file) (blob-addr-to-hex file)))

(defn enter-tree [name addr]
  (cond (nil? addr) nil
        :else (utils/concat-bytes (.getBytes (str "40000 " name "\000")) (utils/from-hex-string addr))))

(defn sort-contents [files]
  (filter #(not (nil? %)) files))

(defn levels? [files]
  (reduce + 0 (map count files)))

(defn sort-alpha [files]
  (sort-by #(.getName %) (rest files)))

(defn find-entries [dir db contents]
  (let [no-nil (sort-contents contents)
        levels (levels? no-nil)
        join-no-nil (apply concat no-nil)
        make-tree-bytes (-> (str "tree " levels "\000") .getBytes (concat join-no-nil) byte-array)]
    (cond (= (count no-nil) 0) nil
          :else (create-object dir db make-tree-bytes))))

(defn files-per-level [level sorted]
  (filter #(= level (+ (count (re-seq #"\\" (.getPath %))) (count (re-seq #"/" (.getPath %))))) sorted))

(defn anything? [%]
  (cond (nil? %) (println "The directory was empty, so nothing was saved.")
        :else (println %)))

(defn build-tree [db dir level current]
  (let [files (file-seq current)
        sorted (sort-alpha files)
        filesOnLevel (files-per-level level sorted)
        contents (for [file filesOnLevel]
                   (if (.isDirectory file)
                     (when (not= (.getName file) db)
                       (enter-tree (.getName file) (build-tree db dir (inc level) file)))
                     (enter-blob file dir db)))]
    (find-entries dir db (vec contents))))

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
            :else (->> (io/file dir)
                       (build-tree (count (re-find (re-pattern "/") dir)) db dir)
                       anything?))
      
      (catch Exception e
        e (println "Error: write-wtree accepts no arguments")))))