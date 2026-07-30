(ns marketentry.store
  "SSoT for the Gambia (GMB) market-entry compliance actor, behind a
  `Store` protocol so the backend is a swap, not a rewrite -- the same
  seam every prior cloud-itonami actor in this fleet uses.

    - `MemStore`     -- atom of EDN. The deterministic default for
                        dev/tests/demo (no deps).
    - `DatomicStore` -- backed by `langchain.db`, a Datomic-API-compatible
                        EAV store, via `langchain-store.core` for the
                        EDN-blob codec + event-log seam (ADR-2607141600
                        -- never a hand-rolled enc/dec* two-liner).

  Both implement the same protocol and pass the same contract
  (test/marketentry/store_contract_test.clj).

  The primary entity here is an `engagement` -- filing-draft and
  filing-submit actuation events apply SEQUENTIALLY to the SAME
  engagement record (draft first, submit later). Dedicated
  double-actuation-guard booleans (`:drafted?`/`:submitted?`, never a
  `:status` value).

  The ledger stays append-only on every backend."
  (:require [marketentry.registry :as registry]
            [langchain.db :as d]
            [langchain-store.core :as ls]
            [marketplace.persist :as persist]))

(defprotocol Store
  (engagement [s id])
  (all-engagements [s])
  (assessment-of [s engagement-id] "committed jurisdiction assessment, or nil")
  (ledger [s])
  (draft-history [s] "the append-only filing-draft history")
  (submit-history [s] "the append-only filing-submit history")
  (next-draft-sequence [s jurisdiction])
  (next-submit-sequence [s jurisdiction])
  (engagement-already-drafted? [s engagement-id])
  (engagement-already-submitted? [s engagement-id])
  (commit-record! [s record] "apply a committed op's record to the SSoT")
  (append-ledger! [s fact]   "append one immutable decision fact")
  (with-engagements [s engagements] "replace/seed the engagement directory"))

;; ----------------------------- demo data -----------------------------

(defn demo-data
  "A small, self-contained engagement set covering both actuation
  lifecycles (draft, submit) plus the governor's own new checks.
  `:seeking-sic?` / `:investor-origin` / `:investment-amount-usd` are
  ground truth for the `sic-ineligible` flagship check (GIEPA Act 2015,
  Special Investment Certificate); `:requires-tin-record?` /
  `:tin-record-verified?` are ground truth for the conditional GRA
  TIN-record check."
  []
  {:engagements
   {"eng-1" {:id "eng-1" :operator "Banjul Fabrication Ltd" :portal "gppa.gm/suppliers"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :seeking-sic? false :investor-origin nil :investment-amount-usd 0.0
             :requires-tin-record? true :tin-record-verified? true
             :drafted? false :submitted? false
             :jurisdiction "GMB" :status :intake}
    "eng-2" {:id "eng-2" :operator "Atlantis LLC" :portal "gppa.gm/suppliers"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :seeking-sic? false :investor-origin nil :investment-amount-usd 0.0
             :requires-tin-record? true :tin-record-verified? true
             :drafted? false :submitted? false
             :jurisdiction "ATL" :status :intake}
    "eng-3" {:id "eng-3" :operator "Kombo Systems Ltd" :portal "gppa.gm/suppliers"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 999000.0
             :seeking-sic? false :investor-origin nil :investment-amount-usd 0.0
             :requires-tin-record? true :tin-record-verified? true
             :drafted? false :submitted? false
             :jurisdiction "GMB" :status :intake}
    "eng-4" {:id "eng-4" :operator "Serrekunda Agro Investments" :portal "gppa.gm/suppliers"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :seeking-sic? true :investor-origin :foreign :investment-amount-usd 50000.0
             :requires-tin-record? true :tin-record-verified? true
             :drafted? false :submitted? false
             :jurisdiction "GMB" :status :intake}
    "eng-5" {:id "eng-5" :operator "Brikama Logistics Ltd" :portal "gppa.gm/suppliers"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :seeking-sic? false :investor-origin nil :investment-amount-usd 0.0
             :requires-tin-record? true :tin-record-verified? false
             :drafted? false :submitted? false
             :jurisdiction "GMB" :status :intake}}})

;; ----------------------------- shared commit logic -----------------------------

(defn- draft-filing!
  [s engagement-id]
  (let [e (engagement s engagement-id)
        seq-n (next-draft-sequence s (:jurisdiction e))
        result (registry/register-draft engagement-id (:jurisdiction e) seq-n)]
    {:result result
     :engagement-patch {:drafted? true
                        :draft-number (get result "draft_number")}}))

(defn- submit-filing!
  [s engagement-id]
  (let [e (engagement s engagement-id)
        seq-n (next-submit-sequence s (:jurisdiction e))
        result (registry/register-submit engagement-id (:jurisdiction e) seq-n)]
    {:result result
     :engagement-patch {:submitted? true
                        :submit-number (get result "submit_number")}}))

;; ----------------------------- MemStore (default) -----------------------------

(defrecord MemStore [a]
  Store
  (engagement [_ id] (get-in @a [:engagements id]))
  (all-engagements [_] (sort-by :id (vals (:engagements @a))))
  (assessment-of [_ engagement-id] (get-in @a [:assessments engagement-id]))
  (ledger [_] (:ledger @a))
  (draft-history [_] (:draft-records @a))
  (submit-history [_] (:submit-records @a))
  (next-draft-sequence [_ jurisdiction] (get-in @a [:draft-sequences jurisdiction] 0))
  (next-submit-sequence [_ jurisdiction] (get-in @a [:submit-sequences jurisdiction] 0))
  (engagement-already-drafted? [_ engagement-id] (boolean (get-in @a [:engagements engagement-id :drafted?])))
  (engagement-already-submitted? [_ engagement-id] (boolean (get-in @a [:engagements engagement-id :submitted?])))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (swap! a update-in [:engagements (:id value)] merge value)

      :assessment/set
      (swap! a assoc-in [:assessments (first path)] payload)

      :engagement/mark-drafted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (draft-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:draft-sequences jurisdiction] (fnil inc 0))
                       (update-in [:engagements engagement-id] merge engagement-patch)
                       (update :draft-records registry/append result))))
        result)

      :engagement/mark-submitted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (submit-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:submit-sequences jurisdiction] (fnil inc 0))
                       (update-in [:engagements engagement-id] merge engagement-patch)
                       (update :submit-records registry/append result))))
        result)
      nil)
    s)
  (append-ledger! [_ fact] (swap! a update :ledger conj fact) fact)
  (with-engagements [s engagements] (when (seq engagements) (swap! a assoc :engagements engagements)) s))

(defn seed-db
  "A MemStore seeded with the demo engagement set."
  []
  (->MemStore (atom (assoc (demo-data)
                           :assessments {}
                           :ledger [] :draft-sequences {} :draft-records []
                           :submit-sequences {} :submit-records []))))

;; ----------------------------- DatomicStore (langchain.db) -----------------------------
;;
;; Uses langchain-store.core (`ls/*`) for the EDN-blob codec + event-log
;; read/append seam, never a hand-rolled enc/dec* (ADR-2607141600).

(def ^:private schema
  {:engagement/id                   {:db/unique :db.unique/identity}
   :assessment/engagement-id        {:db/unique :db.unique/identity}
   :ledger/seq                      {:db/unique :db.unique/identity}
   :draft-record/seq                {:db/unique :db.unique/identity}
   :submit-record/seq               {:db/unique :db.unique/identity}
   :draft-sequence/jurisdiction     {:db/unique :db.unique/identity}
   :submit-sequence/jurisdiction    {:db/unique :db.unique/identity}})

(defn- engagement->tx [{:keys [id operator portal base-fee monthly-rate monitoring-months claimed-fee
                               seeking-sic? investor-origin investment-amount-usd
                               requires-tin-record? tin-record-verified?
                               drafted? submitted?
                               jurisdiction status draft-number submit-number]}]
  (cond-> {:engagement/id id}
    operator                              (assoc :engagement/operator operator)
    portal                                (assoc :engagement/portal portal)
    base-fee                              (assoc :engagement/base-fee base-fee)
    monthly-rate                          (assoc :engagement/monthly-rate monthly-rate)
    monitoring-months                     (assoc :engagement/monitoring-months monitoring-months)
    claimed-fee                           (assoc :engagement/claimed-fee claimed-fee)
    (some? seeking-sic?)                  (assoc :engagement/seeking-sic? seeking-sic?)
    investor-origin                       (assoc :engagement/investor-origin investor-origin)
    (some? investment-amount-usd)         (assoc :engagement/investment-amount-usd investment-amount-usd)
    (some? requires-tin-record?)          (assoc :engagement/requires-tin-record? requires-tin-record?)
    (some? tin-record-verified?)          (assoc :engagement/tin-record-verified? tin-record-verified?)
    (some? drafted?)                      (assoc :engagement/drafted? drafted?)
    (some? submitted?)                    (assoc :engagement/submitted? submitted?)
    jurisdiction                          (assoc :engagement/jurisdiction jurisdiction)
    status                                (assoc :engagement/status status)
    draft-number                          (assoc :engagement/draft-number draft-number)
    submit-number                         (assoc :engagement/submit-number submit-number)))

(def ^:private engagement-pull
  [:engagement/id :engagement/operator :engagement/portal :engagement/base-fee :engagement/monthly-rate
   :engagement/monitoring-months :engagement/claimed-fee
   :engagement/seeking-sic? :engagement/investor-origin :engagement/investment-amount-usd
   :engagement/requires-tin-record? :engagement/tin-record-verified?
   :engagement/drafted? :engagement/submitted?
   :engagement/jurisdiction :engagement/status :engagement/draft-number :engagement/submit-number])

(defn- pull->engagement [m]
  (when (:engagement/id m)
    {:id (:engagement/id m) :operator (:engagement/operator m) :portal (:engagement/portal m)
     :base-fee (:engagement/base-fee m) :monthly-rate (:engagement/monthly-rate m)
     :monitoring-months (:engagement/monitoring-months m) :claimed-fee (:engagement/claimed-fee m)
     :seeking-sic? (boolean (:engagement/seeking-sic? m))
     :investor-origin (:engagement/investor-origin m)
     :investment-amount-usd (:engagement/investment-amount-usd m)
     :requires-tin-record? (boolean (:engagement/requires-tin-record? m))
     :tin-record-verified? (boolean (:engagement/tin-record-verified? m))
     :drafted? (boolean (:engagement/drafted? m)) :submitted? (boolean (:engagement/submitted? m))
     :jurisdiction (:engagement/jurisdiction m) :status (:engagement/status m)
     :draft-number (:engagement/draft-number m) :submit-number (:engagement/submit-number m)}))

(defrecord DatomicStore [conn]
  Store
  (engagement [_ id]
    (pull->engagement (d/pull (d/db conn) engagement-pull [:engagement/id id])))
  (all-engagements [_]
    (->> (d/q '[:find [?id ...] :where [?e :engagement/id ?id]] (d/db conn))
         (map #(pull->engagement (d/pull (d/db conn) engagement-pull [:engagement/id %])))
         (sort-by :id)))
  (assessment-of [_ engagement-id]
    (ls/dec* (d/q '[:find ?p . :in $ ?eid
                   :where [?a :assessment/engagement-id ?eid] [?a :assessment/payload ?p]]
                 (d/db conn) engagement-id)))
  (ledger [_] (ls/read-stream conn :ledger/seq :ledger/fact))
  (draft-history [_] (ls/read-stream conn :draft-record/seq :draft-record/record))
  (submit-history [_] (ls/read-stream conn :submit-record/seq :submit-record/record))
  (next-draft-sequence [_ jurisdiction]
    (or (d/q '[:find ?n . :in $ ?j
              :where [?e :draft-sequence/jurisdiction ?j] [?e :draft-sequence/next ?n]]
            (d/db conn) jurisdiction)
        0))
  (next-submit-sequence [_ jurisdiction]
    (or (d/q '[:find ?n . :in $ ?j
              :where [?e :submit-sequence/jurisdiction ?j] [?e :submit-sequence/next ?n]]
            (d/db conn) jurisdiction)
        0))
  (engagement-already-drafted? [s engagement-id]
    (boolean (:drafted? (engagement s engagement-id))))
  (engagement-already-submitted? [s engagement-id]
    (boolean (:submitted? (engagement s engagement-id))))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (d/transact! conn [(engagement->tx value)])

      :assessment/set
      (d/transact! conn [{:assessment/engagement-id (first path) :assessment/payload (ls/enc payload)}])

      :engagement/mark-drafted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (draft-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))
            next-n (inc (next-draft-sequence s jurisdiction))]
        (d/transact! conn
                     [(engagement->tx (assoc engagement-patch :id engagement-id))
                      {:draft-sequence/jurisdiction jurisdiction :draft-sequence/next next-n}
                      {:draft-record/seq (count (draft-history s)) :draft-record/record (ls/enc (get result "record"))}])
        result)

      :engagement/mark-submitted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (submit-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))
            next-n (inc (next-submit-sequence s jurisdiction))]
        (d/transact! conn
                     [(engagement->tx (assoc engagement-patch :id engagement-id))
                      {:submit-sequence/jurisdiction jurisdiction :submit-sequence/next next-n}
                      {:submit-record/seq (count (submit-history s)) :submit-record/record (ls/enc (get result "record"))}])
        result)
      nil)
    s)
  (append-ledger! [s fact]
    (ls/append-blob! conn :ledger/seq :ledger/fact (count (ledger s)) fact)
    fact)
  (with-engagements [s engagements]
    (when (seq engagements) (d/transact! conn (mapv engagement->tx (vals engagements)))) s))

(defn datomic-store
  ([] (datomic-store {}))
  ([{:keys [engagements]}]
   (let [s (->DatomicStore (d/create-conn schema))]
     (with-engagements s engagements))))

(defn datomic-seed-db
  []
  (datomic-store (demo-data)))

;; ----------------------------- KotobaseStore (the Worker's) -----------------------------

(defrecord KotobaseStore [st seed]
  Store
  (engagement [_ id]
    (when id (persist/get-doc (persist/ctx st :engagement :engagement/id) id)))
  (all-engagements [_]
    (sort-by :id (persist/all-docs (persist/ctx st :engagement :engagement/id))))
  (assessment-of [_ engagement-id]
    (persist/get-doc (persist/ctx st :assessment :assessment/engagement-id) engagement-id))

  (ledger [_] (persist/read-events (persist/stream-ctx st :ledger)))
  (draft-history [_] (persist/read-events (persist/stream-ctx st :draft)))
  (submit-history [_] (persist/read-events (persist/stream-ctx st :submit)))

  ;; MemStore keeps a per-jurisdiction counter; here the count of the stream
  ;; IS that counter. This actor covers exactly one jurisdiction, and every
  ;; draft appends exactly one record, so the two agree - and a length is a
  ;; read where a counter document would be a read-modify-write that two
  ;; concurrent drafts would collide on.
  (next-draft-sequence [s _jurisdiction] (count (draft-history s)))
  (next-submit-sequence [s _jurisdiction] (count (submit-history s)))

  (engagement-already-drafted? [s engagement-id]
    (boolean (:drafted? (engagement s engagement-id))))
  (engagement-already-submitted? [s engagement-id]
    (boolean (:submitted? (engagement s engagement-id))))

  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (let [c (persist/ctx st :engagement :engagement/id)
            existing (persist/get-doc c (:id value))]
        (persist/put-doc! c (merge existing value)))

      :assessment/set
      (persist/put-doc! (persist/ctx st :assessment :assessment/engagement-id)
                        (assoc payload :assessment/engagement-id (first path)))

      :engagement/mark-drafted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (draft-filing! s engagement-id)
            c (persist/ctx st :engagement :engagement/id)]
        (persist/append-event! (persist/stream-ctx st :draft) seed result)
        (persist/put-doc! c (merge (persist/get-doc c engagement-id) engagement-patch))
        result)

      :engagement/mark-submitted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (submit-filing! s engagement-id)
            c (persist/ctx st :engagement :engagement/id)]
        (persist/append-event! (persist/stream-ctx st :submit) seed result)
        (persist/put-doc! c (merge (persist/get-doc c engagement-id) engagement-patch))
        result)
      nil)
    s)

  (append-ledger! [_ fact]
    (persist/append-event! (persist/stream-ctx st :ledger) seed fact)
    fact)

  (with-engagements [s engagements]
    (let [c (persist/ctx st :engagement :engagement/id)]
      (doseq [[_ e] engagements] (persist/put-doc! c e)))
    s))

(defn kotobase-store
  "The durable Store over a HOST-INJECTED database api.

  `marketplace.persist/store` throws when `db-api` is missing or partial, so
  this actor cannot come up looking durable while writing to nothing."
  [{:keys [db-api seq-fn]}]
  (->KotobaseStore (persist/store {:db-api db-api :actor "marketentry-gmb"})
                   (or seq-fn (let [n (atom 0)] #(swap! n inc)))))
