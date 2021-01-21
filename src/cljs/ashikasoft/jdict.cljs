(ns ashikasoft.jdict
  (:require
   [ajax.core :refer [GET POST]]
   [ashikasoft.jdict-common :as common]
   [clojure.string :as string]))

(defn sync-get [url]
  (let [p (promise)]
    (ajax/GET url {:handler #(deliver p %)})
    @p))

(defn read-url-lines [url read-fn]
  (let [body (sync-get url)
        lines (string/split body #"\n")]
    (into [] (map read-fn) lines)))

(defn load-data-dir
  "Create a dictionary instance using the given data directory. "
  [dir]
  (let [res-loader (partial read-url-lines (str dir "/" %))]
    (common/create-dict res-loader)))

(defn lookup
  "Look up a word from the dictionary.
  The dictionary should be initialized with load-data-dir."
  [dict word]
  (common/lookup dict word))
