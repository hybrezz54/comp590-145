(ns utils
  (:require [clojure.java.io :as io])
  (:import java.security.MessageDigest)
  (:import java.util.zip.DeflaterOutputStream
           (java.io ByteArrayInputStream
                    ByteArrayOutputStream))
  (:import java.io.ByteArrayOutputStream
           java.util.zip.InflaterInputStream))

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
  "Unzip the given file's contents with zlib."
  [path]
  (with-open [input (-> path io/file io/input-stream)
              unzipper (InflaterInputStream. input)
              out (ByteArrayOutputStream.)]
    (io/copy unzipper out)
    (.toByteArray out)))

(defn sha-bytes [bytes]
  (.digest (MessageDigest/getInstance "sha1") bytes))

(defn to-hex-string
  "Convert the given byte array into a hex string, 2 characters per byte."
  [bytes]
  (letfn [(to-hex [byte]
            (format "%02x" (bit-and 0xff byte)))]
    (->> bytes (map to-hex) (apply str))))

(defn hex-digits->byte
  [[dig1 dig2]]
  ;; This is tricky because something like "ab" is "out of range" for a
  ;; Byte, because Bytes are signed and can only be between -128 and 127
  ;; (inclusive). So we have to temporarily use an int to give us the room
  ;; we need, then adjust the value if needed to get it in the range for a
  ;; byte, and finally cast to a byte.
  (let [i (Integer/parseInt (str dig1 dig2) 16)
        byte-ready-int (if (< Byte/MAX_VALUE i)
                         (byte (- i 256))
                         i)]
    (byte byte-ready-int)))

(defn from-hex-string
  [hex-str]
  (byte-array (map hex-digits->byte (partition 2 hex-str))))

;; Note that if given binary data this will fail with an error message
;; like:
;; Execution error (IllegalArgumentException) at ,,,.
;; Value out of range for char: -48
(defn bytes->str [bytes]
  (->> bytes (map char) (apply str)))

(defn split-at-byte [b bytes]
  (let [part1 (take-while (partial not= b) bytes)
        part2 (nthrest bytes (-> part1 count inc))]
    [part1 part2]))

(defn string
  "Return the sha1 sum of the data as a 40-character string."
  [data]
  (-> data bytes to-hex-string))

(defn path-maker
  [dir db addy]
  (str dir db "/objects/" (subs addy 0 2) "/" (subs addy 2)))

;; (defn blo-stor
;;   [file dir db]
;;   (let [bNh (str "blob " (count (slurp file)) "\000" (slurp file))
;;         addy (sha1-sum bNh)
;;         path (path-maker dir db addy)]
;;     (cond (not (.exists (io/as-file path)))
;;           (do (io/make-parents path)
;;               (io/copy (zip-str bNh) (io/file path))))))

(defn concat-bytes
  "Concatenate multiple items into a byte array."
  [& byte-arrays]
  (byte-array
   (mapcat cast byte-arrays)))

(defn obj-path [dir db addr]
  (str dir "/" db "/objects/" (subs addr 0 2) "/" (subs addr 2)))