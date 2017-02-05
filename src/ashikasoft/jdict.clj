(ns ^{:doc "Ashikasoft Japanese dictionary library" :author "Kean Santos"}
 ashikasoft.jdict
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(def max-result-count 200)
(def roman-regex #"^\s*[a-zA-Z0-9 ]+\s*$")

(defn roman?
  "Check for roman alphabet (as opposed to japanese input)"
  [word] (re-find roman-regex word))

(defn map-translate
  "Translate a string given a map-like list (order of kv pairs is significant)"
  [s kvs]
  (reduce #(string/replace %1 (first %2) (second %2)) s kvs))

(defn to-hiragana
  "Convert to katakana"
  [dict word]
  (-> (map-translate word (get-in dict [:kana-map :kh]))
      (map-translate (get-in dict [:kana-map :rh]))))

(defn to-katakana
  "Convert to katakana"
  [dict word]
  (-> (map-translate word (get-in dict [:kana-map :hk]))
      (map-translate (get-in dict [:kana-map :rk]))))

(defn read-resource-file
  "Given a directory and a filename, pass a lazy sequence to the given function."
  [dir filename read-fn]
  (with-open [r (io/reader (io/file dir filename))]
    (read-fn (line-seq r))))

(defn reduce-roman-kana-map
  "Add multiple roman keys to the kana map"
  [m h k r]
  (-> (update m :rh conj [r h])
      (update :rk conj [r k])))

(defn reduce-kana-map
  "Reduce sequential data into kana map"
  [kana-map [h k & rs :as row]]
  (let [reduce-roman-fn #(reduce-roman-kana-map %1 h k %2)]
    (as-> kana-map m
      (update m :hk conj [h k])
      (update m :kh conj [k h])
      (reduce reduce-roman-fn m rs))))

(defn load-kana-map-fn
  "Load file contents into roman to hiragana and roman to katakana maps."
  [lines]
  (let [initial-map {:hk [] :rk [] :kh [] :rh []}]
    (->> lines
      (filter #(not (re-matches #"^\s*#.*$" %)))
      (map #(string/split % #","))
      (reduce reduce-kana-map initial-map))))

(defn load-kana-map
  "Load the roman to kana conversion map"
  [dir]
  ;; TODO implement kana map
  (read-resource-file dir "kana_map.csv" load-kana-map-fn))

(defn load-index-tree
  "Load the given index file data into a sorted map.
  The map contains sorted keys corresponding to subfile names
  (see get-subfilenames)."
  [index-data]
  (let [add-index-map #(do (.put %1 (first %2) (last %2)) %1)]
    (->> index-data
      (map #(string/split % #"#"))
      (reduce add-index-map (java.util.TreeMap.)))))

(defn load-index
  "Given a directory and a file pattern, get the contents of an index file
  as a sequence of lines."
  [dir filename-part]
  (read-resource-file dir (str filename-part ".utf8_csort_index") load-index-tree))


(defn load-data-dir
  "Create a dictionary instance using the given data directory.
  This function loads indices into memory as a structure of maps.
  The data directory is organised as follows:
  * Words have definition ids, and definition ids are grouped into subfiles.
  * Each index contains a mapping between words and subfiles.
  * Definition ids from subfiles are used to look up defitions in definition files.
  Separate index files are provided for English (WNet), English (JMDict), Kana and Kanji.
  Also, WNet files contain defitions directly in the subfile (so separate definition file)."
  [dir]
  { :data-dir dir
    :kana-map (load-kana-map dir)
    :en-wnet (load-index dir "wnet_ej")
    :en-jmdict (load-index dir "eng_list")
    :kana-jmdict (load-index dir "kana_list")
    :kanji-jmdict (load-index dir "kanji_list")})

(defn get-subfilenames
  "Given a search word, get a list of files from the sorted map.
  A subfile is a file containing a list of candidate definition ids for a given word."
  [index-map word]
  (loop [entry (.floorEntry index-map word)
          result []]
    (let [key (when entry (.getKey entry))
          value (when key (.getValue entry))
          updated (if value (conj result value) result)]
      (if (and key (or (string/starts-with? key word) (compare key word)))
        (recur (.higherEntry index-map key) updated)
        result))))

(defn remove-delims
  "Remove delimiters from definition"
  [s] (string/replace s "#" " "))

(defn get-def-data
    "Given a definition id, return the definition data."
    [dir id]
    (let [id-part (subs id 0 3)
          filename (str "defs_list.utf8_" id-part)
          get-fn (fn [lines] (->> lines
                               (filter #(string/starts-with? % id))
                               (map #(remove-delims (string/replace-first % #"[0-9]+" "")))
                               (into (sorted-set))))]
      (read-resource-file dir filename get-fn)))

(defn filter-subfile-data
  "Given a search word and a subfile, filter subfile data using the word and delimiter"
  [subfile-data word delim]
  (let [map-fn (if delim
                  #(second (string/split % #"#"))
                  identity)]
    (->> subfile-data
      (filter #(string/starts-with? % word))
      (map map-fn)
      (reduce conj (sorted-set)))))

(defn get-subfile-entries
  "Read the subfile and extract ids matching the word"
  [dir subfile-name word delim]
  (read-resource-file dir subfile-name #(filter-subfile-data % word delim)))

(defn lookup-subfile-entries
  "Look up a word using the given dictionary, keys and delimiter."
  [dict word dict-keys delim]
  (let [dir (:data-dir dict)
        subfile-names (mapcat #(get-subfilenames (% dict) word) dict-keys)]
    (take max-result-count
      (into (sorted-set)
        (mapcat #(get-subfile-entries dir % word delim))
        subfile-names))))

(defn lookup-jmdict
  "Look up a word from a dictionary in the JMDict format."
  [dict word & dict-keys]
  (let [dir (:data-dir dict)
        subfile-entries (lookup-subfile-entries dict word dict-keys #"#")]
    (mapcat #(get-def-data dir %) subfile-entries)))

(defn lookup-wnet
  "Look up a word from the dictionary using the WNet format."
  [dict word]
  (let [subfile-entries (lookup-subfile-entries dict word [:en-wnet] nil)]
    (map remove-delims subfile-entries)))

(defn lookup
  "Look up a word from the dictionary. The dictionary should be initialized with load-data-dir."
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
