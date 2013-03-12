(ns wordcount.pages
  (:require [clojure.data.xml :as xml]
            [clojure.java.io :as io]))

(defn- parent->children [parent tag]
  (filter #(= tag (:tag %)) (:content parent)))

(defn- parent->child [parent tag]
  (first (parent->children parent tag)))

(defn- content [tag]
  (first (:content tag)))

(defn- text [page]
  (let [revision (parent->child page :revision)
        text (parent->child revision :text)]
    (content text)))

(defn get-pages [n filename]
  (let [in (io/reader filename)
        content (:content (xml/parse in))
        pages (take n (filter #(= :page (:tag %)) content))]
    (map text pages)))
