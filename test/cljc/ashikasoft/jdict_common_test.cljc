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
