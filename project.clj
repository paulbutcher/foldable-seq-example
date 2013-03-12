(defproject foldable-seq "1.0"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/data.xml "0.0.7"]]
  :main wordcount.core
  :jvm-opts ["-Xms4G" "-Xmx4G" "-XX:NewRatio=8"])
