(set-env!
 :source-paths    {{{source-paths}}}
 :resource-paths  #{"resources"}
 :dependencies '[[adzerk/boot-cljs      "0.0-2411-5" :scope "test"]
                 [adzerk/boot-cljs-repl "0.1.7"      :scope "test"]
                 [adzerk/boot-reload    "0.2.0"      :scope "test"]{{{deps}}}])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.http         :refer [serve]]{{{requires}}})

(deftask run
  ""
  []
  (comp (serve :dir "target")
        (watch)
        (speak)
        (cljs-repl)
        (cljs :output-to "js/app.js")
        {{{build-steps}}}
        (reload)))

(deftask production
  ""
  []
  (task-options! cljs {:optimizations :advanced}{{{production-task-opts}}})
  (run))

(deftask development
  ""
  []
  (task-options! cljs {:optimizations :none
                       :unified-mode true
                       :source-map true}{{{development-task-opts}}})
  (run))
