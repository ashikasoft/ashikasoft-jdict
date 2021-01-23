(ns ashikasoft.jdict
  (:require
   [ajax.core :refer [GET POST]]
   [ashikasoft.jdict-common :as common]
   [clojure.string :as string]))


(defn load-lines-handler [read-lines-fn body]
  (let [lines (string/split body #"\n")]
    (into [] (map read-fn) lines)))

(defn async-loader [error-fn store-fn read-lines-fn url]
  (ajax/GET url {:handler #(store-fn (load-lines-handler read-lines-fn %))
                 :error-handler error-fn}))

(defn async-state-loader [state store-keys read-lines-fn dir file]
  (let [error-fn #(swap! state assoc :error %)
        store-fn #(swap! state assoc-in store-keys %1)
        loader (fn [keyvec read-fn url]
                 (async-loader error-fn (partial store-fn keyvec) read-fn url))])
  (async-loader error-fn store-fn read-lines-fn (str dir "/" file)))

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
  (let [state-loader (fn [store-keys read-fn file]
                           (async-state-loader state store-keys read-fn dir file))]
    (swap! state assoc :state-loader state-loader)
    (load-kana-map (partial state-loader [:dict :kana-map]))
    (load-index (partial state-loader [:dict :en-wnet]) "wnet_ej")
    (load-index (partial state-loader [:dict :en-wnet]) "wnet_ej")
    (load-index (partial state-loader [:dict :en-jmdict]) "eng_list")
    (load-index (partial state-loader [:dict :kana-jmdict]) "kana_list")
    (load-index (partial state-loader [:dict :kanji-jmdict]) "kanji_list")))

(defn async-lookup
  "Look up a word from the dictionary.
  The dictionary should be initialized with load-data-dir."
  [dict word]
  (when-not (string/blank? word)
    (let [word (string/lower-case (string/trim word))
          hiragana (to-hiragana dict word)
          katakana (to-katakana dict word)]
      (into []
        (concat
          (when (roman? word) (lookup-wnet dict word))
          (into (sorted-set) ;; JMDict entries should be de-duplicated
            (concat
              (if (roman? word)
                (lookup-jmdict dict word :en-jmdict)
                (lookup-jmdict dict word :kana-jmdict :kanji-jmdict))
              (when hiragana (lookup-jmdict dict hiragana :kana-jmdict :kanji-jmdict))
              (when (and katakana (not= katakana hiragana))
                (lookup-jmdict dict katakana :kana-jmdict :kanji-jmdict)))))))))
