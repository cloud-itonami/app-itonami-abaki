(ns itonami.app.entry
  "Abaki on the device (AWAI-3502). Calls abaki.methods.analyze and
  abaki.methods.react-router unchanged. Left out: intel ingest (network),
  live publish (needs the operator's live gate) and the mesh daemon.
  The example entities are the repo's fictional seed."
  (:require [abaki.methods.analyze :as analyze]
            [abaki.methods.react-router :as router]
            [itonami.app.run :as run]))

(def ^:private example-entities
  "{:entities
 [{\"id\" \"entity:compute:megacorp_a\" \"name\" \"MegaCorp AI Compute\" \"domain\" \"compute\"
   \"traits\" {\"closed_source_models\" true \"proprietary_hardware_lockin\" true \"pricing_power_abuse\" true}
   \"beneficial_owners\" [\"individual:tech_baron_x\"]}
  {\"id\" \"entity:biology:agri_monopoly_b\" \"name\" \"GlobalSeeds Inc.\" \"domain\" \"biology\"
   \"traits\" {\"f1_hybrid_lockin\" true \"gene_patents\" true}
   \"beneficial_owners\" [\"individual:agri_baron_y\"]}
  {\"id\" \"entity:compute:murakumo_friendly\" \"name\" \"Open Source Compute Coop\" \"domain\" \"compute\"
   \"traits\" {\"closed_source_models\" false}
   \"beneficial_owners\" [\"coop:members\"]}]}")

(run/register!
 {:name "ci" :label "CI を計算する"
  :help "特徴（trait）ごとの重みを足し、上限 100 で Chokepoint Index を返します。60 を超えると迂回の対象です。"
  :example "{:traits {\"closed_source_models\" true \"pricing_power_abuse\" true \"market_share_dominance\" false}}"
  :run (fn [{:keys [traits]}] {:ci (analyze/calculate-ci traits) :weights analyze/weights})})

(run/register!
 {:name "analyze" :label "事業者を分析する"
  :help "各事業者の CI を計算し、報告（Markdown）と迂回方針（blocked / safe）を作ります。"
  :example example-entities
  :run (fn [{:keys [entities]}]
         (let [{:keys [report-lines routing-policy]} (analyze/analyze entities)]
           {:report (analyze/report-md report-lines) :routing-policy routing-policy}))})

(run/register!
 {:name "route" :label "迂回と生存ツリーを見る"
  :help "分析した事業者から、計算資源の迂回先と、分野ごとの代替（生存ツリー）を示します。"
  :example example-entities
  :run (fn [{:keys [entities]}]
         (let [policy (:routing-policy (analyze/analyze entities))]
           {:compute-routing (router/simulate-murakumo-compute-routing policy)
            :survival-tree (router/simulate-ossekai-survival-tree policy)}))})
