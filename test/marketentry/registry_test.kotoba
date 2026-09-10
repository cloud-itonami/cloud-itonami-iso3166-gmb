(ns marketentry.registry-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.registry :as registry]))

(deftest engagement-fee-recompute
  (let [e {:base-fee 500000 :monthly-rate 30000 :monitoring-months 12 :claimed-fee 860000.0}]
    (is (== 860000.0 (registry/compute-engagement-fee e)))
    (is (true? (registry/engagement-fee-matches-claim? e))))
  (let [bad {:base-fee 500000 :monthly-rate 30000 :monitoring-months 12 :claimed-fee 999000.0}]
    (is (false? (registry/engagement-fee-matches-claim? bad)))))

(deftest register-draft-and-submit
  (let [d (registry/register-draft "eng-1" "GMB" 0)
        s (registry/register-submit "eng-1" "GMB" 0)]
    (is (= "GMB-DFT-000000" (get d "draft_number")))
    (is (= "GMB-SUB-000000" (get s "submit_number")))
    (is (nil? (get-in d ["certificate" "proof"])))
    (is (= "draft-unsigned" (get-in s ["certificate" "status"])))))

(deftest register-requires-ids
  (is (thrown? Exception (registry/register-draft "" "GMB" 0)))
  (is (thrown? Exception (registry/register-submit "eng-1" "" 0))))

(deftest sic-eligible-by-domestic-threshold
  (testing "a domestic investor is eligible at exactly $100,000, not below"
    (is (true? (registry/sic-eligible? {:investor-origin :domestic :investment-amount-usd 100000})))
    (is (true? (registry/sic-eligible? {:investor-origin :domestic :investment-amount-usd 150000})))
    (is (false? (registry/sic-eligible? {:investor-origin :domestic :investment-amount-usd 99999})))))

(deftest sic-eligible-by-foreign-threshold
  (testing "a foreign investor needs the HIGHER $250,000 threshold -- the same investment amount that clears the domestic bar does not clear the foreign one"
    (is (true? (registry/sic-eligible? {:investor-origin :foreign :investment-amount-usd 250000})))
    (is (false? (registry/sic-eligible? {:investor-origin :foreign :investment-amount-usd 249999})))
    (is (false? (registry/sic-eligible? {:investor-origin :foreign :investment-amount-usd 100000}))
        "clears the domestic threshold but NOT the foreign one -- the threshold itself is origin-conditional")))

(deftest sic-eligible-missing-or-unrecognized-origin-fails-closed
  (is (false? (registry/sic-eligible? {:investment-amount-usd 1000000})))
  (is (false? (registry/sic-eligible? {:investor-origin :stateless :investment-amount-usd 1000000})))
  (is (false? (registry/sic-eligible? {}))))

(deftest sic-ineligible-claim-is-entity-scope-gated
  (testing "an engagement NOT declared :seeking-sic? is never flagged, even if it would fail eligibility"
    (is (false? (registry/sic-ineligible-claim? {:seeking-sic? false :investor-origin :foreign :investment-amount-usd 1000}))))
  (testing "a SIC-seeking engagement that fails its own origin's threshold -> ineligible claim"
    (is (true? (registry/sic-ineligible-claim? {:seeking-sic? true :investor-origin :foreign :investment-amount-usd 50000}))))
  (testing "a SIC-seeking engagement that DOES satisfy its own origin's threshold -> not flagged"
    (is (false? (registry/sic-ineligible-claim? {:seeking-sic? true :investor-origin :domestic :investment-amount-usd 120000})))
    (is (false? (registry/sic-ineligible-claim? {:seeking-sic? true :investor-origin :foreign :investment-amount-usd 300000})))))

;; ---------------------------------------------------------------------------
;; Money is compared at money precision, not at double precision
;; ---------------------------------------------------------------------------

(deftest whole-unit-fees-were-already-correct-and-stay-correct
  (testing "the seeded shape: base + rate x months in whole currency units"
    (is (registry/engagement-fee-matches-claim?
         {:base-fee 500000 :monthly-rate 30000 :monitoring-months 12
           :claimed-fee 860000.0}))))

(deftest cent-denominated-fees-are-no-longer-rejected-while-correct
  (testing "`(== (double claimed) (+ (double base) (* (double rate) (double months))))`
            rejected CORRECT totals once an amount carried cents -- 40,989 of
            327,060 combinations (12.5%), against 0 of 327,060 in whole units"
    (let [bad (for [m (range 1 37)
                    bc (range 10000 90000 2100)
                    rc (range 500 6000 210)
                    :let [truth (/ (+ bc (* rc m)) 100.0)]
                    :when (not (registry/engagement-fee-matches-claim?
                                {:base-fee (/ bc 100.0) :monthly-rate (/ rc 100.0)
                                  :monitoring-months m :claimed-fee truth}))]
                [m (/ bc 100.0) (/ rc 100.0) truth])]
      (is (empty? bad) (str "false rejections: " (count bad) " e.g. " (first bad))))))

(deftest a-genuinely-wrong-fee-is-still-caught
  (testing "rounding to money precision must not blunt the check"
    (is (not (registry/engagement-fee-matches-claim?
              {:base-fee 500000 :monthly-rate 30000 :monitoring-months 12
                :claimed-fee 860000.01})))
    (is (not (registry/engagement-fee-matches-claim?
              {:base-fee 500000 :monthly-rate 30000 :monitoring-months 12
                :claimed-fee 859999.99})))))

(deftest an-unverifiable-fee-never-matches
  (testing "un-verifiable is not the same as correct, and not a crash"
    (is (not (registry/engagement-fee-matches-claim?
              {:base-fee 500000 :monthly-rate 30000 :monitoring-months 12})))
    (is (not (registry/engagement-fee-matches-claim?
              {:base-fee "500000" :monthly-rate 30000 :monitoring-months 12
                :claimed-fee 860000.0})))
    (is (nil? (registry/compute-engagement-fee {:base-fee 500000 :monthly-rate 30000})))))
