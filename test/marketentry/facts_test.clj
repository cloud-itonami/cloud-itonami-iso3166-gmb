(ns marketentry.facts-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.facts :as facts]))

(deftest gmb-has-spec-basis
  (let [sb (facts/spec-basis "GMB")]
    (is (some? sb))
    (is (string? (:provenance sb)))
    (is (seq (:required-evidence sb)))
    (is (some? (facts/corporate-number-spec-basis "GMB")))
    (is (some? (facts/sic-spec-basis "GMB")))))

(deftest gmb-rep-spec-basis-is-honestly-absent
  (testing "GPPA's Procurement Act 2022 text sits behind a client-side-rendering access gap this iteration could not read around -- deliberately not claimed"
    (is (nil? (facts/rep-spec-basis "GMB")))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest required-evidence-satisfied
  (let [sb (facts/spec-basis "GMB")
        all (:required-evidence sb)]
    (is (true? (facts/required-evidence-satisfied? "GMB" all)))
    (is (not (facts/required-evidence-satisfied? "GMB" (take 1 all))))
    (is (nil? (facts/required-evidence-satisfied? "ATL" all)))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["GMB" "ATL" "ZZZ"])]
    (is (= 3 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["GMB"] (:covered-jurisdictions c)))
    (is (= ["ATL" "ZZZ"] (:missing-jurisdictions c)))))

(deftest catalog-is-gambia-only
  (testing "no unlabeled foreign-jurisdiction contamination (scaffold-copy incident)"
    (is (= #{"GMB"} (set (keys facts/catalog))))))

(deftest sic-spec-basis-criteria
  (let [sic (facts/sic-spec-basis "GMB")]
    (is (= 100000 (get-in sic [:sic-criteria :min-investment-usd-domestic])))
    (is (= 250000 (get-in sic [:sic-criteria :min-investment-usd-foreign])))))
