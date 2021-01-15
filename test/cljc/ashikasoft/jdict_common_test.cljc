(ns ashikasoft.jdict-common-test
  (:require
   [clojure.string :as string]
   [clojure.test :refer [deftest is testing]]
   [ashikasoft.jdict-common :refer :all]))

(def test-resources
  {"kana_map.csv"
   "
"} )

(defn test-res-loader [_ filename load-fn]
  (-> (get test-resources filename)
      (string/split #"¥n")
      load-fn))

(deftest test-roman?
  (testing "Anything with roman characters or numerals is roman."
    (is (roman? "Parisian")))
  (testing "If there are no roman characters, it isn't roman."
    (is (not (roman? "日本人")))))
