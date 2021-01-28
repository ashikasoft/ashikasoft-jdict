(defproject ashikasoft/jdict "1.0.1"
  :description "Clojure implementation of Ashikasoft Japanese Dictionary"
  :url "http://www.ashikasoft.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.avl "0.1.0"]]
  :source-paths ["src/clj"
                 "src/cljc"
                 "src/cljs"]
  :test-paths ["test/cljc"])
