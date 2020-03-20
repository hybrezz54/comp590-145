(ns idiot
  (:require [clojure.java.io :as io])
  (:import java.security.MessageDigest)
  (:import java.util.zip.DeflaterOutputStream
           (java.io ByteArrayInputStream
                    ByteArrayOutputStream))
  (:import java.io.ByteArrayOutputStream
           java.util.zip.InflaterInputStream))

;************** HELPER FUNCTIONS ********************

(defn sha1-hash-bytes [data]
  (.digest (MessageDigest/getInstance "sha1")
           (.getBytes data)))

(defn byte->hex-digits [byte]
  (format "%02x"
          (bit-and 0xff byte)))

(defn bytes->hex-string [bytes]
  (->> bytes
       (map byte->hex-digits)
       (apply str)))

(defn sha1-sum [header+blob]
  (bytes->hex-string (sha1-hash-bytes header+blob)))

(defn zip-str
  "Zip the given data with zlib. Return a ByteArrayInputStream of the zipped
  content."
  [data]
  (let [out (ByteArrayOutputStream.)
        zipper (DeflaterOutputStream. out)]
    (io/copy data zipper)
    (.close zipper)
    (ByteArrayInputStream. (.toByteArray out))))

(defn unzip
  "Unzip the given data with zlib. Pass an opened input stream as the arg. The
  caller should close the stream afterwards."
  [input-stream]
  (with-open [unzipper (InflaterInputStream. input-stream)
              out (ByteArrayOutputStream.)]
    (io/copy unzipper out)
    (->> (.toByteArray out)
         (map char)
         (apply str))))

;************** IMPLEMENTED FUNCTIONS ********************

(defn help
  "Print command usage info"
  [n]
  (let [flag (first n)]
    (try
      (if (and (not= flag nil) (not= flag "-h") (not= flag "--help") (not= flag "help")
               (not= flag "init") (not= flag "hash-object") (not= flag "cat-file")
               (not= flag "write-wtree"))
        (throw (Exception.)) ())

      (if (or (= flag "help") (= flag "init") (= flag "hash-object") (= flag "cat-file")
              (= flag "write-wtree"))
        ((load-string (str "idiot/" flag)) ["-h"])
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
                  (println "Usage: idiot <command> [<args>]")
                  (println)))

            (println "Commands:")
            (println "   help")
            (println "   init")
            (println "   hash-object [-w] <file>")
            (println "   cat-file -p <address>")))
      (catch Exception e
        e (println "Error: invalid command")))))

(defn init
  "initialize a new database"
  [n]
  (let [flag (first n)]
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
        (if (.exists (io/file ".git"))
          (println "Error: .git directory already exists")
          (do (.mkdirs (io/file ".git/objects"))
              (println "Initialized empty Idiot repository in .git directory"))))

      (catch Exception e
        e (println "Error: init accepts no arguments")))))

(defn hash-object
  "compute address and maybe create blob from file"
  [n]
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
                                           address (sha1-sum header-plus-blob)
                                           dir (subs address 0 2)
                                           name (subs address 2)]
                                       (cond (= flag "-w")
                                             (do (.mkdirs (io/as-file (str ".git/objects/" dir)))
                                                 (io/copy (zip-str header-plus-blob)
                                                          (io/file (str ".git/objects/" dir "/" name)))))
                                       (println address))
            :else (println "Error: that file isn't readable"))

      (catch Exception e
        e (println "Error: you must specify a file.")))))

(defn cat-file
  "print information about an object"
  [n]
  (let [flag (first n)
        addr (last n)]
    (try
      (if (and (not= flag "-h") (not= flag "--help") (not= flag "-p") (= (first flag) "-"))
        (throw (Exception.)) ())

      (cond (or (= flag "-h") (= flag "--help"))
            (do (println "idiot cat-file: print information about an object")
                (println)
                (println "Usage: idiot cat-file -p <address>")
                (println)
                (println "Arguments:")
                (println "   -h          print this message")
                (println "   -p          pretty-print contents based on object type")
                (println "   <address>   the SHA1-based address of the object"))
            (not (.exists (io/file ".git"))) (println "Error: could not find database. (Did you run `idiot init`?)")
            (not (= flag "-p")) (println "Error: the -p switch is required")
            (and (= flag "-p") (= flag addr)) (throw (Exception.))
            :else (let [dir (subs addr 0 2)
                        name (subs addr 2)]
                    (if (.exists (io/file (str ".git/objects/" dir "/" name)))
                      (let [blob (with-open [input (-> (str ".git/objects/" dir "/" name) io/file io/input-stream)] (unzip input))
                            blob-start (.indexOf blob "\000")]
                        (print (subs blob (+ blob-start 1))))
                      (println "Error: that address doesn't exist"))))

      (catch Exception e
        e (println "Error: you must specify an address")))))

(defn write-wtree
  "write the working tree to the database"
  [n]
  (let [flag (first n)]
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
            (not (.exists (io/file ".git"))) (println "Error: could not find database. (Did you run `idiot init`?)")
            :else (println "Do something"))

      (catch Exception e
        e (println "Error: write-wtree accepts no arguments")))))

(defn -main [& args]
  (cond (= (count args) 0) (help nil)
        (and (or (= (first args) "help") (= (first args) "-h") (= (first args) "--help"))) (help (rest args))
        (and (= (first args) "init") (< (count (rest args)) 2)) (init (rest args))
        (and (= (first args) "hash-object") (< (count (rest args)) 3)) (hash-object (rest args))
        (and (= (first args) "cat-file") (< (count (rest args)) 3)) (cat-file (rest args))
        (and (= (first args) "write-wtree") (< (count (rest args)) 2)) (write-wtree (rest args))
        :else (println "Error: invalid command")))