# cloud-itonami-iso3166-gmb

**GMB**: The Gambia.

- GPPA (Gambia Public Procurement Authority) / Procurement Act 2022
  public-procurement compliance
- Companies Department (Ministry of Justice) business/company
  registration + Gambia Revenue Authority (GRA) TIN registration
- GIEPA (Gambia Investment & Export Promotion Agency) Act 2015 Special
  Investment Certificate (SIC) minimum-investment eligibility gate

AGPL-3.0-or-later.

## Market-entry / statute catalogs

Governed public-sector market-entry compliance actor, same architecture
as every `cloud-itonami-iso3166-*` sibling in this fleet:

- `src/marketentry/{facts,governor,phase,sim,operation,registry,store,
  marketentryllm}.cljc` -- the actor. `facts.cljc` cites the Gambia
  Public Procurement Authority (GPPA, Procurement Act 2022), the
  Companies Department of the Ministry of Justice (business/company
  registration, own published fee schedule tiered by declared share
  capital), and the Gambia Revenue Authority (GRA, Taxpayer
  Identification Number registration, established by Act No. 13 of
  2004 as amended by Act No. 10 of 2010). `governor.cljc`'s flagship
  check independently recomputes whether an engagement's own declared
  investment amount satisfies the GIEPA Act 2015 Special Investment
  Certificate (SIC) minimum-investment threshold for its own declared
  investor origin ($100,000 domestic / $250,000 foreign) -- a flat
  threshold whose OWN VALUE is conditional on the bidder's declared
  identity, a check shape genuinely different from every other
  iso3166 sibling's (see the namespace docstrings for the full
  research trail and honestly-narrowed scope, including facts this
  iteration could NOT verify, such as GPPA's detailed Procurement Act
  2022 provisions and a domestic Companies Act citation).
- `src/statute/facts.kotoba` -- general-law catalog: the Labour Act,
  2023 (Ministry of Trade, Industry, Regional Integration &
  Employment) and the Income and Value Added Tax Act (2012, Gambia
  Revenue Authority, year corroborated independently from GIEPA's own
  incentives page). A Companies Act citation could not be
  independently verified this iteration (an honest gap, not an
  omission by design; see the namespace docstring).

Every citation is curl/WebFetch-verified against an official source
(gppa.gm, moj.gm, gra.gm, giepa.gm, motie.gov.gm); GPPA's own site is a
client-side-rendered Next.js/base44 application whose detailed
Procurement Act 2022 text and supplier registration-requirements
checklist returned only a 'Loading...' HTML shell to both `curl` and
`WebFetch` this iteration -- an honestly-flagged ACCESS gap, not a
claim of non-existence, see `marketentry.facts`'s docstring. The
Labour Act, 2023 PDF was downloaded directly and independently
cross-checked (extracted text vs. a rendered page image) to confirm it
is a genuine, current, machine-readable document, despite a stale
'Labour Act of 2007' reference remaining on the same ministry's own
separate Employment directorate page -- see `statute.facts`'s
docstring for this honestly-flagged site inconsistency.

## Culture catalog

Alongside the market-entry / statute catalogs, this repo carries a
**country-level regional-culture catalog** (ADR-2607171400 addendum 2,
`cloud-itonami-municipality-culture-catalog` Wave 1, in
`com-junkawasaki/root`) — national dishes, protected products, beverages,
crafts, festivals and heritage sites for the Gambia:

- `src/culture/facts.kotoba` — the catalog, source of truth (keyed by
  uppercase ISO3, mirroring `statute.facts`).
- `schema/culture.edn` — DataScript schema.
- `data/culture-tx.edn` — derived DataScript tx-data (regenerated from
  the catalog, never hand-edited).

City-level counterparts live in the `cloud-itonami-municipality-*` repos.
Same provenance discipline as the compliance catalogs: every entry cites a
source URL that was actually fetched and read on `:culture/retrieved-at`;
summaries state only what the cited source confirms. An item not in
`culture.facts/catalog` has no spec-basis — never fabricate one.
