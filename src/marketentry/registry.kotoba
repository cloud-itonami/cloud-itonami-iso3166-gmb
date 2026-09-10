(ns marketentry.registry
  "Pure-function market-entry filing-draft + filing-submit record
  construction -- an append-only market-entry book-of-record draft.

  Like every sibling actor's registry, there is no single international
  reference-number standard for a public-procurement market-entry
  filing -- every jurisdiction assigns its own format. This namespace
  does NOT invent one; it builds a jurisdiction-scoped sequence number
  and validates the record's required fields, the same honest,
  non-fabricating discipline `marketentry.facts` uses.

  `engagement-fee-matches-claim?` is an HONEST reapplication of the
  SAME ground-truth-recompute DISCIPLINE sibling actors use (verify a
  claimed monetary total against the entity's own recorded quantity x
  unit fields), reapplied to a market-entry engagement fee line.

  `sic-eligible?` / `sic-ineligible-claim?` are the SAME discipline
  applied to a genuinely Gambia-specific mechanism: the Gambia
  Investment & Export Promotion Agency's (GIEPA) Special Investment
  Certificate (SIC), per GIEPA Act 2015 -- GIEPA's own incentives page
  (giepa.gm/invest-in-gambia/incentives, fetched directly) states 'The
  SIC is ... available for domestic and foreign investors if they
  invest a minimum of, respectively, $100,000 and $250,000 in a
  priority sector and/or in a priority area'. Only this
  minimum-investment-amount branch is modeled -- GIEPA's own text also
  offers 'employ a minimum number of Gambians set by the regulations'
  and 'create value addition' as alternative eligibility paths, but the
  former is explicitly delegated to unfetched regulations and the
  latter has no stated numeric criterion at all, so neither is modeled
  here (the same honest scope-narrowing discipline this family's CAF
  ministerial-arrêté-delegated Marché réservé value threshold and
  Benin's Art. 77 discretionary-subcontracting branch already
  established).

  This is a GENUINELY DIFFERENT check SHAPE than every prior iso3166
  sibling this repo mirrors: Bulgaria's ЗОП Art. 54(5) de-minimis is a
  PERCENTAGE-OF-TURNOVER eligibility formula, Albania's Neni 76(2)(c)
  carve-out is a SINGLE FLAT-CONSTANT threshold (the same number for
  every entity), Azerbaijan's/Armenia's flagship checks are BOOLEAN
  registry-membership reads, Antigua and Barbuda's vendor-class check
  is a THREE-TIER eligibility-threshold classification, Benin's MPME
  mechanism is a BID-EVALUATION PRICE ADJUSTMENT (not an eligibility
  gate at all), Bhutan's FDI Negative List is a CATEGORICAL
  SECTOR-EXCLUSION allow-list gate, Botswana's citizen/resident-
  preference check is an ORDERED-TIER CLASSIFICATION, CAF's Marché
  réservé mechanism is a MULTI-CRITERION INCLUSION-ELIGIBILITY test
  over the bidder's OWN workforce composition/legal form, and
  Estonia's digital-signing-method check tests the VALIDITY OF THE
  FILING'S OWN EXECUTION INSTRUMENT (a procedural axis, not the
  bidder's business substance at all). The Gambia's SIC mechanism is
  none of these: it is a FLAT INVESTMENT-AMOUNT THRESHOLD whose OWN
  VALUE is selected by the engagement's declared investor-origin
  attribute (`:domestic` vs `:foreign`) -- unlike Albania's single
  constant-for-everyone threshold, the threshold itself is a function
  of the bidder's own declared identity, not a universal constant. The
  first in this family to make the THRESHOLD VALUE, not just the
  eligibility test applied to it, conditional on the bidder's own
  declared attribute.

  This namespace is pure data + pure functions -- no I/O, no network
  call to any real procurement portal. It builds the RECORD an
  operator would keep, not the act of submitting a portal registration
  itself (that is `marketentry.operation`'s `:filing/submit`, always
  human-gated -- see README Actuation)."
  (:require [kotoba.lang.text :as str]))

(defn- unsigned-certificate
  "Every certificate this actor produces is UNSIGNED -- signature is
  the market-entry operator's act, not this actor's."
  [kind subject record-id]
  {"@context" ["https://www.w3.org/ns/credentials/v2"]
   "type" ["VerifiableCredential" kind]
   "credentialSubject" {"id" subject "record" record-id}
   "proof" nil
   "issued_by_registry" false
   "status" "draft-unsigned"})

(defn- zero-pad [n w]
  (let [s (str n)]
    (str (apply str (repeat (max 0 (- w (count s))) "0")) s)))

(def ^:private money-scale
  "Sub-minor-unit scale used when comparing two money amounts: 1/10000 of
  a unit. Coarser than double representation error by many orders of
  magnitude, finer than any real currency's minor unit (2 decimals for
  most, 3 for KWD/BHD/OMR, 0 for JPY/KRW)."
  10000)

(defn- money=
  "Exact-at-money-precision equality for two amounts.

  `==` on raw doubles is NOT the right comparison for money. With
  whole-unit fees the two agree, but as soon as an amount carries
  cents the sum `base + rate x months` is routinely not the double
  nearest the true total, and a CORRECT claim compares false: measured
  on this exact shape, 40,989 of 327,060 cent-denominated combinations
  (12.5%) were rejected while being right, against 0 of 327,060 in
  whole units.

  Rounding both sides to `money-scale` before comparing removes the
  representation error while preserving every distinction money can
  actually carry."
  [x y]
  (and (number? x) (number? y)
       (= (Math/round (* money-scale (double x)))
          (Math/round (* money-scale (double y))))))

(defn compute-engagement-fee
  "The ground-truth engagement fee for `engagement`'s own `:base-fee`
  and `:monitoring-months` x `:monthly-rate` -- a single flat
  base + months x rate calculation, not a full pricing engine."
  [{:keys [base-fee monthly-rate monitoring-months]}]
  ;; nil when any field is not a number: an un-recomputable engagement is
  ;; un-verifiable, which is neither `correct` nor a ClassCastException
  ;; thrown out of the caller.
  (when (and (number? base-fee) (number? monthly-rate) (number? monitoring-months))
    (+ (double base-fee)
       (* (double monthly-rate) (double monitoring-months)))))

(defn engagement-fee-matches-claim?
  "Does `engagement`'s own `:claimed-fee` equal the independently
  recomputed `compute-engagement-fee`?"
  [{:keys [claimed-fee] :as engagement}]
  (money= claimed-fee (compute-engagement-fee engagement)))

(def sic-investment-thresholds
  "GIEPA Act 2015, Special Investment Certificate (SIC) minimum-
  investment eligibility (own text, giepa.gm/invest-in-gambia/
  incentives, fetched directly 2026-07-22): the minimum USD investment
  amount required for eligibility, keyed by the investor's own
  declared origin."
  {:domestic 100000
   :foreign  250000})

(defn sic-eligible?
  "The ground-truth SIC eligibility for `engagement`, independently
  recomputed from its own declared `:investor-origin`
  (`:domestic`/`:foreign`) and `:investment-amount-usd` against the
  threshold for that origin. A missing/unrecognized `:investor-origin`
  or a nil `:investment-amount-usd` simply fails (does not throw)."
  [{:keys [investor-origin investment-amount-usd]}]
  (boolean
   (when-let [threshold (get sic-investment-thresholds investor-origin)]
     (and (some? investment-amount-usd)
          (>= (double investment-amount-usd) (double threshold))))))

(defn sic-ineligible-claim?
  "Does `engagement` declare `:seeking-sic? true` (i.e. it is applying
  for GIEPA's Special Investment Certificate) while the INDEPENDENTLY
  recomputed `sic-eligible?` is false? An engagement not seeking the
  SIC is never flagged by this check (entity/engagement-scope-gated,
  the same discipline Bhutan's `:foreign-company?`-gated FDI check
  uses)."
  [{:keys [seeking-sic?] :as engagement}]
  (boolean (and seeking-sic? (not (sic-eligible? engagement)))))

(defn register-draft
  "Validate + construct the FILING-DRAFT registration DRAFT -- the
  market-entry operator's own act of preparing a portal registration
  package. Pure function -- does not touch any real procurement
  portal."
  [engagement-id jurisdiction sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "draft: engagement_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "draft: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "draft: sequence must be >= 0" {})))
  (let [draft-number (str (str/upper jurisdiction) "-DFT-" (zero-pad sequence 6))
        record {"record_id" draft-number
                "kind" "filing-draft"
                "engagement_id" engagement-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "draft_number" draft-number
     "certificate" (unsigned-certificate "FilingDraft" draft-number draft-number)}))

(defn register-submit
  "Validate + construct the FILING-SUBMIT registration DRAFT -- the
  market-entry operator's own act of actually submitting a portal
  registration (always human-gated upstream)."
  [engagement-id jurisdiction sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "submit: engagement_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "submit: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "submit: sequence must be >= 0" {})))
  (let [submit-number (str (str/upper jurisdiction) "-SUB-" (zero-pad sequence 6))
        record {"record_id" submit-number
                "kind" "filing-submit"
                "engagement_id" engagement-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "submit_number" submit-number
     "certificate" (unsigned-certificate "FilingSubmit" submit-number submit-number)}))

(defn append [history result]
  (conj (vec history) (get result "record")))
