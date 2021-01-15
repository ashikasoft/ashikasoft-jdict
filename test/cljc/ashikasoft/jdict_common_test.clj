(ns ashikasoft.jdict-common-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [ashikasoft.jdict-common :refer :all]))

(deftest test-roman?
  (testing "Anything with roman characters or numerals is roman."
    (is (roman? "Parisian")))
  (testing "If there are no roman characters, it isn't roman."
    (is (not (roman? "日本人")))))
