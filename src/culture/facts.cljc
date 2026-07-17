(ns culture.facts
  "Country-level regional-culture catalog for the Gambia (GMB) -- national
  dishes, protected products, beverages, crafts, festivals and heritage
  sites, per ADR-2607171400 addendum 2 (cloud-itonami-municipality-
  culture-catalog Wave 1, in com-junkawasaki/root). Sibling namespace to
  `marketentry.facts` / `statute.facts` (ADR-2607141700); city-level
  counterparts live in the cloud-itonami-municipality-* repos.

  Catalog is keyed by UPPERCASE ISO3 (mirrors `statute.facts`); entries
  carry no :culture/municipality (that attribute is city-level only).

  Every entry cites a source URL that was actually fetched and read on
  :culture/retrieved-at -- never fabricated. Summaries state only what the
  cited source confirms. An item not in this table has NO spec-basis, full
  stop; extend `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of culture entries."
  {"GMB"
   [{:culture/id "gmb.dish.domoda"
     :culture/name "Domoda"
     :culture/country "GMB"
     :culture/kind :dish
     :culture/summary "The national dish of the Gambia, a stew made with peanut paste."
     :culture/url "https://en.wikipedia.org/wiki/Gambian_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "gmb.dish.benachin"
     :culture/name "Benachin"
     :culture/country "GMB"
     :culture/kind :dish
     :culture/summary "Gambian one-pot rice dish, a slightly different version of thieboudienne, traditionally cooked in one pot."
     :culture/url "https://en.wikipedia.org/wiki/Gambian_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "gmb.dish.yassa"
     :culture/name "Yassa"
     :culture/country "GMB"
     :culture/kind :dish
     :culture/summary "Lemon whole-chicken or fish dish, part of Gambian cuisine."
     :culture/url "https://en.wikipedia.org/wiki/Gambian_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "gmb.dish.supakanja"
     :culture/name "Supakanja"
     :culture/country "GMB"
     :culture/kind :dish
     :culture/summary "Gambian okra stew or soup made with palm oil."
     :culture/url "https://en.wikipedia.org/wiki/Gambian_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "gmb.beverage.attaya"
     :culture/name "Attaya"
     :culture/country "GMB"
     :culture/kind :beverage
     :culture/summary "Sweet green tea, triple-brewed, a common beverage in the Gambia."
     :culture/url "https://en.wikipedia.org/wiki/Gambian_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "gmb.beverage.wonjo"
     :culture/name "Wonjo"
     :culture/country "GMB"
     :culture/kind :beverage
     :culture/summary "Sweet Gambian hibiscus drink made by steeping the roselle fruit."
     :culture/url "https://en.wikipedia.org/wiki/Gambian_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "gmb.heritage.kunta-kinteh-island"
     :culture/name "Kunta Kinteh Island"
     :culture/country "GMB"
     :culture/kind :heritage
     :culture/summary "Island in the Gambia River, 30 km from the river mouth, listed as a UNESCO World Heritage Site for its significance in West African slave-trade history."
     :culture/url "https://en.wikipedia.org/wiki/Kunta_Kinteh_Island"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "gmb.heritage.senegambian-stone-circles"
     :culture/name "Senegambian stone circles"
     :culture/country "GMB"
     :culture/kind :heritage
     :culture/summary "Megalithic stone circles located in the Gambia north of Janjanbureh and in central Senegal, inscribed on the UNESCO World Heritage List in 2006."
     :culture/url "https://en.wikipedia.org/wiki/Senegambian_stone_circles"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}]})

(defn spec-basis [iso3] (get catalog iso3))

(defn coverage
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-gmb culture catalog "
                 "(ADR-2607171400 addendum 2, Wave 1): " (count (get catalog "GMB"))
                 " GMB entries, each with a fetched-and-read citation. "
                 "Extend `culture.facts/catalog`, never fabricate an id/url.")})))

(defn by-kind [iso3 kind]
  (filterv #(= (:culture/kind %) kind) (spec-basis iso3)))
