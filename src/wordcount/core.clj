(ns wordcount.core
  (:require [wordcount.pages :refer :all]
            [wordcount.words :refer :all]
            [clojure.core.reducers :as r]
            [wordcount.reducers :as wr]))

(defn frequencies-parallel [pages]
  (r/fold (partial merge-with +)
          (fn [counts x] (assoc counts x (inc (get counts x 0))))
          (r/flatten (r/map get-words pages))))

(defn -main [& args]
  (time 
    (frequencies-parallel
      (r/flatten
        (r/map get-words 
          (wr/foldable-seq 10 (get-pages 10000 "enwiki.xml"))))))
  nil)
