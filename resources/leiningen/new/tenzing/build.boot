(set-env!
 :source-paths    {{{source-paths}}}
 :resource-paths  #{"resources"}
 :dependencies '[[adzerk/boot-cljs      "0.0-2629-8" :scope "test"]
                 [adzerk/boot-cljs-repl "0.1.7"      :scope "test"]
                 [adzerk/boot-reload    "0.2.3"      :scope "test"]
                 [pandeiro/boot-http    "0.5.2"      :scope "test"]{{{deps}}}])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]{{{requires}}})

(deftask build []
  (comp (speak)
        {{{pre-build-steps}}}
        (cljs :output-to "js/app.js")
        {{{build-steps}}}))

(deftask run []
  (comp (serve)
        (watch)
        (cljs-repl)
        (reload)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced}{{{production-task-opts}}})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none
                       :unified-mode true
                       :source-map true}{{{development-task-opts}}})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))
