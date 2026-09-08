(ns statute.facts-test
  (:require [kotoba.lang.text :as str]
            [clojure.test :refer [deftest is]]
            [statute.facts :as facts]))

(deftest gmb-has-spec-basis
  (let [sb (facts/spec-basis "GMB")]
    (is (= 2 (count sb)))
    (is (every? #(str/starts-with? (:statute/url %) "https://") sb))
    (is (every? :statute/law-number sb))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["GMB" "JPN" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["ATL" "JPN"] (:missing-jurisdictions c)))))

(deftest by-topic-filters
  (is (= ["gmb.labour-act-2023"]
         (mapv :statute/id (facts/by-topic "GMB" :labor))))
  (is (= ["gmb.income-and-vat-act-2012"]
         (mapv :statute/id (facts/by-topic "GMB" :tax))))
  (is (empty? (facts/by-topic "GMB" :corporate-governance))
      "no Companies Act citation could be independently verified this iteration -- honestly absent, see namespace docstring")
  (is (empty? (facts/by-topic "ATL" :labor))))
