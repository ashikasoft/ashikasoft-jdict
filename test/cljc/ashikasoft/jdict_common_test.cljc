(ns ashikasoft.jdict-common-test
  (:require
   [clojure.string :as string]
   [clojure.test :refer [deftest is testing]]
   [ashikasoft.test-resources :as resources]
   [ashikasoft.jdict-common :as common]))


(defn res-loader [load-fn filename]
  (as-> filename $
      (get resources/dict-data $)
      (string/split $ #"\n")
      (load-fn $)))

(deftest test-roman?
  (testing "Anything with roman characters or numerals is roman."
    (is (common/roman? "Parisian")))
  (testing "If there are no roman characters, it isn't roman."
    (is (not (common/roman? "日本人")))))

(deftest test-kana-conversion
  (let [dict {:kana-map (common/load-kana-map res-loader)}]
    (testing "Given a resource loader, build a kana map."
      (is (= #{:hk :rk :rh :kh}
             (->> dict :kana-map keys (into #{})))))
    (testing "Translate a roman word into hiragana."
      (is (= "おにぎり を たべちゃった"
             (common/to-hiragana dict "onigiri wo tabechatta"))))
    (testing "Translate a roman word into katakana."
      (is (= "インターナショナル スクール"
             (common/to-katakana dict "inta-nashonaru suku-ru"))))))

(deftest test-lookup-single-dictionary
  (testing "Loading a top-level index returna a map of lead entries to sub files."
    (let [top-index (common/load-index res-loader "test_dict")
          test-dict {:res-loader res-loader
                     :test-dict top-index}]
      (is (= ["aa" "bb" "cc"] (keys top-index)))
      (testing "Searching an exact match returns a result."
        (let [word "bc"
              result (common/lookup-subfile-entries test-dict word [:test-dict] nil)]
          (is (= ["bc#ビーシー"] result))))
      (testing "Searching an partial match returns multiple results."
        (let [word "b"
              result (common/lookup-subfile-entries test-dict word [:test-dict] nil)]
          (is (= ["bb#ババ" "bc#ビーシー"] result))))
      (testing "Searching a missing term returns an empty result."
        (let [word "x"
              result (common/lookup-subfile-entries test-dict word [:test-dict] nil)]
          (is (= [] result)))))))
