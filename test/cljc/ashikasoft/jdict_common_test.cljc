(ns ashikasoft.jdict-common-test
  (:require
   [clojure.string :as string]
   [clojure.test :refer [deftest is testing]]
   [ashikasoft.test-resources :as resources]
   [ashikasoft.jdict-common :refer :all]))


(defn res-loader [filename load-fn]
  (as-> filename $
      (get resources/dict-data $)
      (string/split $ #"\n")
      (load-fn $)))

(deftest test-roman?
  (testing "Anything with roman characters or numerals is roman."
    (is (roman? "Parisian")))
  (testing "If there are no roman characters, it isn't roman."
    (is (not (roman? "日本人")))))

(deftest test-kana-conversion
  (let [dict {:kana-map (load-kana-map res-loader)}]
    (testing "Given a resource loader, build a kana map."
      (is (= #{:hk :rk :rh :kh}
             (->> dict :kana-map keys (into #{})))))
    (testing "Translate a roman word into hiragana."
      (is (= "おにぎり を たべちゃった"
             (to-hiragana dict "onigiri wo tabechatta"))))
    (testing "Translate a roman word into katakana."
      (is (= "インターナショナル スクール"
             (to-katakana dict "inta-nashonaru suku-ru"))))))

(deftest test-index-subfiles
  (testing "Loading a top-level index returna a map of lead entries to sub files."
    (let [top-index (load-index res-loader "test_dict")]
      (is (= ["aa" "bb" "cc"] (keys top-index)))
      ;; TODO set up test file data and fix broken test
      #_
      (testing "Searching a top-level index returns a filename for a dictionary subset."
        (let [test-dict {:test-dict top-index}
              result (lookup-subfile-entries test-dict "b1" [:test-dict] nil)]
          (is (= :??? result)))))))
