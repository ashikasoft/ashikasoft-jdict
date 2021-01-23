(ns ashikasoft.jdict
  (:require [ashikasoft.file :as file]
            [ashikasoft.jdict-common :as common]))

(defn load-data-dir
  "Create a dictionary instance using the given data directory. "
  [dir]
  (let [res-loader (fn [read-fn file] (file/read-dir-file read-fn dir file))]
    (common/create-dict res-loader)))

(defn lookup
  "Look up a word from the dictionary.
  The dictionary should be initialized with load-data-dir."
  [dict word]
  (common/lookup dict word))
