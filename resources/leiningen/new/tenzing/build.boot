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
 '[boot.pod              :as pod]
 '[boot.util             :as util]
 '[boot.core             :as core]{{{requires}}})

(deftask serve
  "Start a web server on localhost and serve a directory.

   If no directory is specified the current one is used.  Listens on
   port 3000 by default."
  [d dir  PATH str "The directory to serve."
   p port PORT int "The port to listen on."]
  (let [worker (pod/make-pod {:dependencies '[[ring/ring-jetty-adapter "1.3.1"]
                                              [compojure "1.2.1"]]})
        dir    (or dir ".")
        port   (or port 3000)]
    (core/cleanup
     (util/info "<< stopping Jetty... >>")
     (pod/eval-in worker (.stop server)))
    (with-pre-wrap
      (pod/eval-in worker
        (require '[ring.adapter.jetty :refer [run-jetty]]
                 '[compojure.handler  :refer [site]]
                 '[compojure.route    :refer [files]])
        (def server (run-jetty (files "/" {:root ~dir}) {:port ~port :join? false})))
      (util/info "<< started web server on http://localhost:%d (serving: %s) >>\n" port dir))))

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
