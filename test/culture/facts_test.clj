(ns culture.facts-test
  (:require [clojure.edn :as edn]
            [kotoba.lang.text :as str]
            [clojure.test :refer [deftest is]]
            [culture.facts :as facts]))

(deftest gmb-has-culture-basis
  (let [sb (facts/spec-basis "GMB")]
    (is (= 8 (count sb)))
    (is (= (count sb) (count (set (map :culture/id sb)))))
    (is (every? #(str/starts-with? (:culture/url %) "https://") sb))
    (is (every? #(= "GMB" (:culture/country %)) sb))
    (is (every? #(nil? (:culture/municipality %)) sb))
    (is (every? #(seq (:culture/summary %)) sb))
    (is (every? #(string? (:culture/retrieved-at %)) sb))))

(deftest unknown-jurisdiction-has-no-basis
  (is (nil? (facts/spec-basis "SEN")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["GMB" "SEN"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["SEN"] (:missing-jurisdictions c)))))

(deftest by-kind-filters
  (is (= 4 (count (facts/by-kind "GMB" :dish))))
  (is (= ["gmb.heritage.kunta-kinteh-island" "gmb.heritage.senegambian-stone-circles"]
         (mapv :culture/id (facts/by-kind "GMB" :heritage))))
  (is (empty? (facts/by-kind "GMB" :other)))
  (is (empty? (facts/by-kind "SEN" :dish))))

(deftest tx-file-matches-catalog
  (let [tx (edn/read-string (slurp "data/culture-tx.edn"))
        flat (mapcat val (sort-by key facts/catalog))]
    (is (= (vec flat) (vec tx)))))
