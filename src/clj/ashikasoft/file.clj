(ns ashikasoft.file
  (:require
   [clojure.java.io :as io]))

(defn read-lines
  "Read a file line by line, using the given function."
  [read-fn file]
  (with-open [r (io/reader file)]
    (read-fn (line-seq r))))

(defn read-resource-file
  "Given a directory and a filename, read line by line using the given function."
  [read-fn filename]
  (read-lines read-fn (io/file (io/resource filename))))

(defn read-dir-file
  "Given a directory and a filename, read line by line using the given function."
  [read-fn dir filename]
  (read-lines read-fn (io/file dir filename)))

