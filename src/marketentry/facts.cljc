(ns marketentry.facts
  "Per-jurisdiction public-procurement market-entry regulatory catalog
  -- the G2-style spec-basis table the Market-Entry Compliance Governor
  checks every `:jurisdiction/assess` proposal against ('did the advisor
  cite an OFFICIAL public source for this jurisdiction's requirements,
  or did it invent one?').

  The Gambia's real market-entry surface (curl/WebFetch-verified
  2026-07-22; where a page could not be reached, or turned out to be
  client-side-rendered with no readable content, that is stated
  explicitly rather than silently omitted):

  - **Public procurement** is regulated by the Gambia Public
    Procurement Authority (GPPA, `gppa.gm`, fetched directly). GPPA's
    own homepage (fetched directly, plain HTML, not a summary) reads:
    'The Gambia Public Procurement Authority is committed to ensuring
    transparency, efficiency, and value for money in all public
    procurement processes.' GPPA's own navigation and its
    `/resources/procurement-act` page both independently name
    'Procurement Act 2022' as the current governing legislation (this
    iteration confirmed this directly, the same discipline as every
    other Act cited below). HOWEVER: `gppa.gm` is a client-side-rendered
    Next.js/base44 application -- every attempt this iteration made to
    read the ACTUAL text of the Procurement Act 2022, or the detailed
    contents of GPPA's own `/suppliers/registration-requirements` page
    (the supplier/bidder document checklist), returned only a static
    HTML shell containing nav links and the literal string 'Loading...'
    to BOTH plain `curl` and `WebFetch` -- the real content is fetched
    client-side after page load and is not present in the server-
    rendered HTML this iteration was able to read. This is an HONEST,
    explicitly-flagged ACCESS gap (a technical rendering limitation of
    this iteration's fetch tools against this specific site), not a
    claim that the Act or the registration-requirements page do not
    exist -- both are confirmed to exist by name/URL directly from
    GPPA's own site.
  - **Business/company registration** is handled by the Companies
    Department, an operating division of the Ministry of Justice
    (`moj.gm/companies-division`, fetched directly, ordinary
    server-rendered HTML with full content -- unlike GPPA's site).
    Its own text reads: 'The Companies Department under the Ministry of
    Justice is responsible for registering various categories of
    businesses and charitable bodies in The Gambia.' This iteration
    independently confirmed the Companies Department is a SEPARATE
    department from the similarly-named 'Registrar General's
    Department' (`moj.gm/registrar-general-s-department`, also fetched
    directly) -- the Registrar General's Department administers ONLY
    Intellectual Property (patents, utility models, industrial designs,
    trademarks) under the Industrial Property Act 2007 ('Section 3 of
    the Industrial Property Act 2007 defines a patent as a \"title
    granted to protect an invention\"', its own text, own Section
    numbers cited directly), NOT company/business registration. This
    catalog does not conflate the two departments -- the same
    non-conflating discipline this family's CAF (ARMP/DGMP) and Benin
    (ARMP/DNCMP) catalogs use for their own two-body splits. The
    Companies Department's own page publishes an EXACT fee schedule
    this iteration read directly (Dalasi, D): a Name Reservation Fee of
    D500, a Business Registration Fee of D1,000, and a company
    INCORPORATION fee tiered by the company's own declared share
    capital: up to D500,000 -> D10,000; D500,000 to D1,000,000 ->
    D15,000; D1,000,000 to D10,000,000 -> D20,000; above D10,000,000 ->
    D25,000. Registration requires form SWR7 (companies/partnerships)
    or SWR3 (sole proprietorships), Articles & Memorandum of
    Association, and -- for every registration category -- 'A copy of
    the TIN Card' (its own text), i.e. registrants must already hold a
    Gambia Revenue Authority Taxpayer Identification Number before
    registering.
  - **Tax/TIN registration** is administered by the Gambia Revenue
    Authority (GRA, `gra.gm`, fetched directly). GRA's own Legal and
    Board Services Department page (`gra.gm/legal`, fetched directly)
    states: 'The Gambia Revenue Authority was established under section
    3 of the Gambia Revenue Authority Act by Act No. 13 of 2004 and
    amended by Act No. 10 of 2010', and lists the revenue laws it
    administers by name: 'The GRA Act; The Customs and Excise Act; The
    Income and Value Added Tax Act; The Payroll Tax Act; and The Stamp
    Act' (First Schedule), plus a Second Schedule that separately names
    'The Single Window Business Registration Act' -- this iteration
    found this Act's TITLE only (on GRA's own administered-laws list),
    could not independently fetch its own primary text this iteration,
    and does NOT claim to know whether it corresponds to the 'PRO'
    one-stop business-registration mechanism sometimes referenced for
    the Gambia; an honestly-flagged gap, not resolved by guessing.
    GRA's own Domestic Taxes FAQ page (`gra.gm/domestic-faqs`, fetched
    directly) states concrete, current thresholds/rates this iteration
    read directly: VAT registration is compulsory at 'taxable supplies
    of at least D2,000,000 in a tax year' (voluntary from D1,000,000),
    and 'The Corporation tax rate is the higher of 27% of net profit,
    or 1% of turnover on audited accounts (or 2% of turnover for
    unaudited accounts)'.
  - `sic-spec-basis` grounds this vertical's FLAGSHIP check (see
    `marketentry.governor` / `marketentry.registry`) -- a genuinely
    Gambia-specific mechanism this iteration found directly on the
    Gambia Investment & Export Promotion Agency's (GIEPA, `giepa.gm`)
    own 'Incentives' page (`giepa.gm/invest-in-gambia/incentives`,
    fetched directly): 'In accordance with the GIEPA Act 2015 ... the
    following priority sectors and regions of The Gambia are eligible
    to receive incentives.' Its own text names the Special Investment
    Certificate (SIC) as 'the main incentive scheme', stating directly:
    'The SIC is ... available for domestic and foreign investors if
    they invest a minimum of, respectively, $100,000 and $250,000 in a
    priority sector and/or in a priority area, employ a minimum number
    of Gambians set by the regulations, or create value addition.' This
    catalog models ONLY the minimum-investment-amount branch (a
    concrete, unconditional, undelegated number stated directly in
    GIEPA's own text); the 'employ a minimum number of Gambians set by
    the regulations' and 'create value addition' alternative
    eligibility branches are DELIBERATELY NOT modeled -- the former is
    explicitly delegated to unfetched regulations, and the latter has
    no stated numeric criterion at all in this source. This is the same
    honest scope-narrowing discipline this family's CAF (ministerial
    arrêté-delegated Marché réservé value threshold) and Benin (Art. 77
    discretionary-subcontracting branch) catalogs already established.
    GIEPA's incentives page ALSO independently corroborates the year of
    GRA's tax act as 2012 ('notwithstanding the rate provided in
    Schedule III of the Income and Value Added Tax (VAT) Act (2012)'),
    filling a gap GRA's own `/legal` page left open (it names the Act
    but not its year).
  - This iteration also looked for a Gambia-specific representative/
    director exclusion-extension provision (the shape Bulgaria's ЗОП
    Art. 54(2)-(3) / Benin's Art. 61/62 document for their own laws).
    Because GPPA's Procurement Act 2022 text is behind the
    client-side-rendering gap described above, this iteration could NOT
    confirm whether such a provision exists in Gambia's own Procurement
    Act 2022. Rather than assume one exists or invent an article
    number, `rep-spec-basis` below is left honestly nil for GMB, the
    same discipline Bhutan's and CAF's own catalogs use when a
    mechanism's current, citable shape could not be confirmed.
  - This iteration additionally looked for a Gambia-specific 'Companies
    Act' citation (a domestic company-law statute, since -- unlike
    OHADA member states such as CAF/Benin -- the Gambia is a common-law
    jurisdiction with its own company-registration regime, not a
    supranational Uniform Act). Neither the Companies Department's own
    page nor the Registrar General's Department's own page names a
    Companies Act by year/number; this iteration did not find and does
    not cite one. This is an honest gap (see also `statute.facts`,
    which for the same reason carries no companies-law entry for GMB).

  Coverage is reported HONESTLY (see `coverage`): a jurisdiction not in
  this table has NO spec-basis, full stop -- the advisor must not
  fabricate one, and the governor holds if it tries.")

(def catalog
  "iso3 -> requirement map. `:required-evidence` mirrors the generic
  intake/portal-registration/filing evidence set; `:legal-basis` /
  `:owner-authority` / `:provenance` are the G2 citation the governor
  requires before any `:jurisdiction/assess` proposal can commit. GMB
  deliberately carries NO `:rep-owner-authority` -- see the namespace
  docstring's honest-scope-narrowing note (GPPA's Procurement Act 2022
  text is behind a client-side-rendering access gap this iteration
  could not read around). `:sic-owner-authority` / `:sic-legal-basis` /
  `:sic-criteria` / `:sic-provenance` ground this vertical's flagship
  governor check (`sic-eligible?`/`sic-ineligible-claim?` in
  `marketentry.registry`)."
  {"GMB" {:name "The Gambia"
          :owner-authority "Gambia Public Procurement Authority (GPPA) -- 'The Gambia Public Procurement Authority is committed to ensuring transparency, efficiency, and value for money in all public procurement processes' (gppa.gm, own homepage text, fetched directly)"
          :legal-basis "Procurement Act 2022 (named directly by GPPA's own site navigation and its own /resources/procurement-act page; the Act's detailed section text and GPPA's own supplier registration-requirements checklist sit behind a client-side-rendered Next.js/base44 application that returned only a 'Loading...' shell to both curl and WebFetch this iteration -- an honestly-flagged ACCESS gap, not a claim of non-existence, see namespace docstring)"
          :national-spec "Business/company registration: Companies Department, Ministry of Justice (moj.gm/companies-division, own text: 'The Companies Department under the Ministry of Justice is responsible for registering various categories of businesses and charitable bodies in The Gambia'; own published fee schedule: Name Reservation D500, Business Registration D1,000, incorporation fee tiered by declared share capital -- up to D500,000: D10,000; D500,000-D1,000,000: D15,000; D1,000,000-D10,000,000: D20,000; above D10,000,000: D25,000). NOTE: the similarly-named Registrar General's Department is a SEPARATE division of the same Ministry handling ONLY Intellectual Property under the Industrial Property Act 2007 -- not conflated with company registration here. Tax/TIN registration: Gambia Revenue Authority (GRA), established 'under section 3 of the Gambia Revenue Authority Act by Act No. 13 of 2004 and amended by Act No. 10 of 2010' (gra.gm/legal, own text), administering 'The Income and Value Added Tax Act; The Payroll Tax Act; and The Stamp Act' among others"
          :provenance "https://gppa.gm/ ; https://gppa.gm/resources/procurement-act ; https://moj.gm/companies-division ; https://moj.gm/registrar-general-s-department ; https://www.gra.gm/legal ; https://www.gra.gm/domestic-faqs"
          :required-evidence ["Business Registration Certificate (Companies Department, Ministry of Justice -- form SWR7 for companies/partnerships, SWR3 for sole proprietorships, per moj.gm/companies-division, fetched directly)"
                              "TIN Card copy (Gambia Revenue Authority -- moj.gm/companies-division's own registration requirements list 'a copy of the TIN Card' for every registration category, confirming GRA-issued TINs are a precondition of business registration itself)"
                              "Certificate of Incorporation (Companies Department, Ministry of Justice, per GIEPA's own 'How to Invest' step-by-step page, giepa.gm/invest-in-gambia/how-to-invest, fetched directly)"
                              "GPPA Supplier Registration confirmation (Supplier Registration Portal exists per GPPA's own navigation, gppa.gm/suppliers/registration, but this iteration could NOT independently read the portal's detailed document checklist -- a client-side-rendered page, see namespace docstring; the confirmation record itself is still required evidence, its detailed contents are simply not modeled here)"
                              "Operational License for the relevant sector, where applicable (GIEPA's own 'How to Invest' page names this as a required step for regulated sectors -- energy, ICT, fisheries, agriculture, financial services, tourism -- each issued by a different sector ministry/regulator)"
                              "Special Investment Certificate (SIC) eligibility confirmation record, when the engagement declares :seeking-sic? true"]
          :corporate-number-owner-authority "Gambia Revenue Authority (GRA)"
          :corporate-number-legal-basis "GRA's own Legal and Board Services Department page (gra.gm/legal, fetched directly): 'The Gambia Revenue Authority was established under section 3 of the Gambia Revenue Authority Act by Act No. 13 of 2004 and amended by Act No. 10 of 2010', administering (per its own First Schedule list) 'The GRA Act; The Customs and Excise Act; The Income and Value Added Tax Act; The Payroll Tax Act; and The Stamp Act'"
          :corporate-number-provenance "https://www.gra.gm/legal"
          :sic-owner-authority "Gambia Investment & Export Promotion Agency (GIEPA)"
          :sic-legal-basis "GIEPA Act 2015 (giepa.gm/invest-in-gambia/incentives, own text, fetched directly): 'In accordance with the GIEPA Act 2015, the following priority sectors and regions of The Gambia are eligible to receive incentives.' Own definition of the Special Investment Certificate (SIC): 'The SIC is the main incentive scheme and is available for domestic and foreign investors if they invest a minimum of, respectively, $100,000 and $250,000 in a priority sector and/or in a priority area, employ a minimum number of Gambians set by the regulations, or create value addition.' Only the minimum-investment-amount branch is modeled -- the workforce/value-addition alternative branches are delegated/unquantified in this source and deliberately not modeled, see namespace docstring"
          :sic-criteria {:min-investment-usd-domestic 100000
                         :min-investment-usd-foreign 250000}
          :sic-provenance "https://www.giepa.gm/invest-in-gambia/incentives"}
   "USA" {:name "United States"
          :owner-authority "U.S. General Services Administration (GSA) / SAM.gov"
          :legal-basis "Federal Acquisition Regulation (FAR); System for Award Management"
          :national-spec "SAM.gov entity registration + NAICS self-certification"
          :provenance "https://sam.gov/"
          :required-evidence ["EIN record"
                              "SAM.gov registration record"
                              "State business registration record"
                              "Authorized-representative record"]}
   "DEU" {:name "Germany"
          :owner-authority "Beschaffungsamt des BMI / e-Vergabe platforms"
          :legal-basis "Gesetz gegen Wettbewerbsbeschränkungen (GWB) / VgV"
          :national-spec "e-Vergabe supplier registration under EU procurement directives"
          :provenance "https://www.evergabe-online.de/"
          :required-evidence ["Handelsregister extract"
                              "e-Vergabe registration record"
                              "USt-IdNr record"
                              "Authorized-representative record"]}})

(defn spec-basis
  "The jurisdiction's requirement map, or nil -- nil means NO spec-basis,
  and the governor must hold any proposal that tries to assess or file
  on it."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report: how many of the requested jurisdictions actually
  have a spec-basis entry. Never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-gmb R0: " (count catalog)
                 " jurisdictions seeded with an official spec-basis. "
                 "This is a starting catalog for market-entry navigation, "
                 "not a survey of all ~194 jurisdictions -- extend "
                 "`marketentry.facts/catalog`, never fabricate a "
                 "jurisdiction's requirements.")})))

(defn required-evidence-satisfied?
  "Does `submitted` (a set/coll of evidence keywords or strings) satisfy
  every evidence item listed for `iso3`? Missing spec-basis -> never
  satisfied."
  [iso3 submitted]
  (when-let [{:keys [required-evidence]} (spec-basis iso3)]
    (let [need (count required-evidence)
          have (count (filter (set submitted) required-evidence))]
      (= need have))))

(defn evidence-checklist [iso3]
  (:required-evidence (spec-basis iso3) []))

(defn rep-spec-basis
  "The jurisdiction's representative-related requirement map, or nil when
  this catalog has no such regime. For GMB this is deliberately nil --
  see the `catalog` docstring's honest-scope-narrowing note (GPPA's
  Procurement Act 2022 text sits behind a client-side-rendering access
  gap this iteration could not read around, so no representative/
  director exclusion-extension provision could be confirmed)."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:rep-owner-authority sb)
      (select-keys sb [:rep-owner-authority :rep-legal-basis :rep-provenance]))))

(defn corporate-number-spec-basis
  "The jurisdiction's corporate-number / tax-id regime, or nil."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:corporate-number-owner-authority sb)
      (select-keys sb [:corporate-number-owner-authority
                       :corporate-number-legal-basis
                       :corporate-number-provenance]))))

(defn sic-spec-basis
  "The jurisdiction's Special Investment Certificate (SIC) minimum-
  investment eligibility regime, or nil. For GMB this is real and
  current -- the flagship check this vertical adds is grounded here
  (GIEPA Act 2015, Special Investment Certificate)."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:sic-owner-authority sb)
      (select-keys sb [:sic-owner-authority
                       :sic-legal-basis
                       :sic-criteria
                       :sic-provenance]))))
