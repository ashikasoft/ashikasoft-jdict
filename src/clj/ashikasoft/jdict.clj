(ns ashikasoft.jdict
  (:require [ashikasoft.jdict-common :as common]
            [clojure.java.io :as io]))

(defn read-resource-file
  "Given a directory and a filename, pass a lazy sequence to the given function."
  [dir filename read-fn]
  (with-open [r (io/reader (io/file dir filename))]
    (read-fn (line-seq r))))

(defn load-data-dir
  "Create a dictionary instance using the given data directory. "
  [dir]
  (let [res-loader (partial read-resource-file dir)]
    (common/create-dict res-loader)))

(defn lookup
  "Look up a word from the dictionary.
  The dictionary should be initialized with load-data-dir."
  [dict word]
  (common/lookup dict word))
