(ns ashikasoft.jdict
  (:require
   [ajax.core :refer [GET]]
   [ashikasoft.jdict-common :as common]
   [clojure.string :as string]))


(defn async-get [url]
  (js/Promise.
   (fn [resolve reject] (GET url {:handler resolve :error-handler reject}))))

(defn load-lines-handler [read-lines-fn body]
  (let [lines (string/split body #"\n")]
    (into [] (map read-lines-fn) lines)))

(defn async-dir-loader [read-lines-fn dir file]
  (-> (async-get (str dir "/" file))
      (.then #(load-lines-handler read-lines-fn %))))

(defn async-create-dict
  "Create a dictionary instance using the given data resource loader.
  This function loads indices into memory as a structure of maps.
  The data directory is organised as follows:
  * Words have definition ids, and definition ids are grouped into subfiles.
  * Each index contains a mapping between words and subfiles.
  * Definition ids from subfiles are used to look up defitions in definition files.
  Separate index files are provided for English (WNet), English (JMDict), Kana and Kanji.
  Also, WNet files contain defitions directly in the subfile (so separate definition file)."
  [state dir]
  (let [async-loader
        (fn [read-fn file] (-> (async-dir-loader read-fn dir file)
                               (.catch #(swap! state assoc :error %))))
        store-in
        (fn [keyvec val] (swap! state assoc-in keyvec val))]
    (swap! state assoc :async-loader async-loader)
    (-> (common/load-kana-map async-loader)
        (.then (partial store-in [:dict :kana-map])))
    (-> (common/load-index async-loader "wnet_ej")
        (.then (partial store-in [:dict :en-wnet])))
    (-> (common/load-index async-loader "eng_list")
        (.then (partial store-in [:dict :en-jmdict])))
    (-> (common/load-index async-loader "kana_list")
        (.then (partial store-in [:dict :kana-jmdict])))
    (-> (common/load-index async-loader "kanji_list")
        (.then (partial store-in [:dict :kanji-jmdict])))))

#_ (comment
     ;; Lookup using promises:
(lookup dict word [en-wnet])
;; get-subfilenames -- OK
;; get-subfile-entries
     )

(defn async-lookup
  "Look up a word from the dictionary.
  The dictionary should be initialized with load-data-dir."
  [state dict word]
  (when-not (string/blank? word)
    (let [append-results #(swap! state update :results (fnil conj [])%)
          word (string/lower-case (string/trim word))
          hiragana (common/to-hiragana dict word)
          katakana (common/to-katakana dict word)]
      (swap! state dissoc :results)
      (when (common/roman? word)
        (-> (common/lookup-wnet dict word)
            (.then append-results)))
      (if (common/roman? word)
        (-> (common/lookup-jmdict dict word :en-jmdict)
            (.then append-results))
        (-> (common/lookup-jmdict dict word :kana-jmdict :kanji-jmdict)
            (.then append-results)))
      (when hiragana
        (-> (common/lookup-jmdict dict hiragana :kana-jmdict :kanji-jmdict)
            (.then append-results)))
      (when (and katakana (not= katakana hiragana))
        (-> (common/lookup-jmdict dict katakana :kana-jmdict :kanji-jmdict)
            (.then append-results))))))
