(ns wordcount.words)

(defn get-words [text]
  (re-seq #"\w+" text))
