(set-env!
 :source-paths    {{{source-paths}}}
 :resource-paths  #{"resources"}
 :dependencies '[[adzerk/boot-cljs          "2.0.0"      :scope "test"]
                 [adzerk/boot-cljs-repl     "0.3.3"      :scope "test"]
                 [adzerk/boot-reload        "0.5.1"      :scope "test"]
                 [pandeiro/boot-http        "0.8.3"      :scope "test"]
                 [com.cemerick/piggieback   "0.2.1"      :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.13"     :scope "test"]
                 [weasel                    "0.7.0"      :scope "test"]
                 [org.clojure/clojurescript "1.9.562"]{{{deps}}}])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]{{{requires}}})

(deftask build []
  (comp (speak)
        {{{pre-build-steps}}}
        (cljs)
        {{{build-steps}}}))

(deftask run []
  (comp (serve)
        (watch)
        (cljs-repl)
        {{{run-steps}}}
        (reload)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced}{{{production-task-opts}}})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none}
                 reload {:on-jsload '{{name}}.app/init}{{{development-task-opts}}})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))

{{{tasks}}}
