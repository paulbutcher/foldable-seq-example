(defproject foldable-seq "1.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.xml "0.0.7"]
                 [foldable-seq "0.2"]]
  :main wordcount.core
  :jvm-opts ["-Xms4G" "-Xmx4G" "-XX:NewRatio=8"])
