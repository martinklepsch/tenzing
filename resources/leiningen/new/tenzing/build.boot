(set-env!
 :src-paths    {{{source-paths}}}
 :rsc-paths    #{"resources"}
 :dependencies '[[adzerk/boot-cljs      "0.0-2371-27" :scope "test"]
                 [adzerk/boot-cljs-repl "0.1.6"       :scope "test"]
                 [adzerk/boot-reload    "0.1.6"       :scope "test"]{{{deps}}}])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.http         :refer [serve]]
 '[boot.pod              :as pod]
 '[boot.util             :as util]
 '[boot.core             :as core]{{{requires}}})

(deftask run
  ""
  []
  (comp (serve :dir "target/public")
        (watch)
        (speak)
        (cljs-repl)
        (cljs :output-to "public/js/app.js")
        {{{build-steps}}}
        (reload)))

(deftask production
  ""
  []
  (core/task-options! cljs [:optimizations :advanced]{{{production-task-opts}}})
  (run))

(deftask development
  ""
  []
  (core/task-options! cljs [:optimizations :none
                            :unified true
                            :source-maps true]{{{development-task-opts}}})
  (run))
