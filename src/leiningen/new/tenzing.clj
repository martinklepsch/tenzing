(ns leiningen.new.tenzing
  (:require [leiningen.new.templates :as t :refer [name-to-path ->files sanitize slurp-resource]]
            [leiningen.core.main :as main]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]))

;; potentially write a function here that can be called like (prettify
;; (render ...))  which goes through the files forms and pprints each
;; toplevel form separated by a blank line
;; http://stackoverflow.com/questions/6840425/with-clojure-read-read-string-function-how-do-i-read-in-a-clj-file-to-a-list-o

;; (defn prettify [s]
;;   (io/reader
;;    (apply str
;;           (drop-last (rest (with-out-str (fipp s)))))))

;; (defn renderer
;;   "Create a renderer function that looks for mustache templates in the
;;   right place given the name of your template. If no data is passed, the
;;   file is simply slurped and the content returned unchanged."
;;   [name]
;;   (fn [template & [data]]
;;     (let [path (string/join "/" ["leiningen" "new" (t/sanitize name) template])]
;;       (if-let [resource (io/resource path)]
;;         (if data
;;           (prettify
;;            (t/render-text (t/slurp-resource resource) data))
;;           (io/reader resource))
;;         (main/abort (format "Template resource '%s' not found." path))))))

(def render (t/renderer "tenzing"))

; Next three functions are copied from chestnut
(defn wrap-indent [wrap n list]
  (fn []
    (->> list
         (map #(str "\n" (apply str (repeat n " ")) (wrap %)))
         (string/join ""))))

(defn dep-list [n list]
  (wrap-indent #(str "[" % "]") n list))

(defn indent [n list]
  (wrap-indent identity n list))

;; -------------------------------------------------------------------------
;; Options - currently: reagent, om, om-next, sass, garden, devtools, dirac
;; -------------------------------------------------------------------------

(defn reagent? [opts]
  (some #{"+reagent"} opts))

(defn om? [opts]
  (some #{"+om"} opts))

(defn om-next? [opts]
  (some #{"+om-next"} opts))

(defn rum? [opts]
  (some #{"+rum"} opts))

(defn sass? [opts]
  (some #{"+sass"} opts))

(defn garden? [opts]
  (some #{"+garden"} opts))

(defn less? [opts]
  (some #{"+less"} opts))

(defn test? [opts]
  (some #{"+test"} opts))

(defn devtools? [opts]
  (some #{"+devtools"} opts))

(defn dirac? [opts]
  (some #{"+dirac"} opts))

(defn boot-cljs-devtools? [opts]
  (or
   (devtools? opts)
   (dirac? opts)))

;; ---------------------------------------------------------------
;; Template data helpers
;; ---------------------------------------------------------------

(defn source-paths [opts]
  (cond-> #{"src/cljs"}
          (garden? opts) (conj "src/clj")
          (sass? opts)   (conj "sass")
          (less? opts)   (conj "less")))

(defn dependencies [opts]
  (cond-> []
          (test?     opts)           (conj "crisptrutski/boot-cljs-test \"0.3.0\" :scope \"test\"")
          (om?       opts)           (conj "org.omcljs/om \"0.8.6\"")
          (om-next?  opts)           (conj "org.omcljs/om \"1.0.0-alpha47\"")
          (rum?      opts)           (conj "rum \"0.10.7\"")
          (reagent?  opts)           (conj "reagent \"0.6.0\"")
          (garden?   opts)           (conj "org.martinklepsch/boot-garden \"1.3.2-0\" :scope \"test\"")
          (sass?     opts)           (conj "deraen/boot-sass  \"0.3.0\" :scope \"test\"")
          (sass?     opts)           (conj "org.slf4j/slf4j-nop  \"1.7.21\" :scope \"test\"")
          (less?     opts)           (conj "deraen/boot-less \"0.6.0\" :scope \"test\"")
          (devtools? opts)           (conj "binaryage/devtools \"0.9.0\" :scope \"test\"")
          (dirac?    opts)           (conj "binaryage/dirac \"1.1.3\" :scope \"test\"")
          (boot-cljs-devtools? opts) (conj "powerlaces/boot-cljs-devtools \"0.2.0\" :scope \"test\"")))

(defn build-requires [opts]
  (cond-> []
          (test?     opts)           (conj "'[crisptrutski.boot-cljs-test :refer [test-cljs]]")
          (garden?   opts)           (conj "'[org.martinklepsch.boot-garden :refer [garden]]")
          (sass?     opts)           (conj "'[deraen.boot-sass :refer [sass]]")
          (less?     opts)           (conj "'[deraen.boot-less :refer [less]]")
          (boot-cljs-devtools? opts) (conj "'[powerlaces.boot-cljs-devtools :refer [cljs-devtools dirac]]")))

(def test-tasks
"(deftask testing []
  (set-env! :source-paths #(conj % \"test/cljs\"))
  identity)

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(ns-unmap 'boot.user 'test)

(deftask test []
  (comp (testing)
        (test-cljs :js-env :phantom
                   :exit?  true)))

(deftask auto-test []
  (comp (testing)
        (watch)
        (test-cljs :js-env :phantom)))")

(defn build-tasks [opts]
                                        ;FIXME: remove with test-tasks
  (cond-> []
          (test? opts) (conj test-tasks)))

                                        ;FIXME: remove
;; (defn pre-build-steps [name opts]
;;   (cond-> []
;;           ))

(defn build-steps [name opts]
  (cond-> []
          (garden? opts) (conj (str "(garden :styles-var '" name ".styles/screen\n:output-to \"css/garden.css\")"))
          (sass?   opts) (conj (str "(sass)"))
          (less?   opts) (conj (str "(less)"))
          (less?   opts) (conj (str "(sift   :move {#\"less.css\" \"css/less.css\" #\"less.main.css.map\" \"css/less.main.css.map\"})"))))

(defn run-steps [name opts]
  (cond-> []
          (devtools? opts) (conj "(cljs-devtools)")
          (dirac? opts) (conj "(dirac)")))

(defn production-task-opts [opts]
  (cond-> []
          (garden? opts) (conj (str "garden {:pretty-print false}"))
          (sass?   opts) (conj (str "sass   {:output-style :compressed}"))
          (less?   opts) (conj (str "less   {:compression true}"))))

(defn development-task-opts [opts]
                                        ;FIXME: change to if
  (cond-> []
    (less? opts) (conj (str "less   {:source-map  true}"))))

(defn index-html-head-tags [opts]
  (letfn [(style-tag [href] (str "<link href=\"" href "\" rel=\"stylesheet\" type=\"text/css\" media=\"screen\">"))]
                                        ;FIXME: rename all styles to styles.css
    (cond-> []
            (garden? opts) (conj (style-tag "css/garden.css"))
            (sass? opts)   (conj (style-tag "css/sass.css"))
            (less? opts)   (conj (style-tag "css/less.css")))))

(defn index-html-script-tags [opts]
  (letfn [(script-tag [src] (str "<script type=\"text/javascript\" src=\"" src "\"></script>"))]
                                        ;FIXME: move to index.html without this
    (cond-> []
            :finally (conj (script-tag "js/app.js")))))

(defn template-data [name opts]
  {:name                   name
   :sanitized              (name-to-path name)
   :source-paths           (source-paths opts)
                                        ;FIXME: only test task here
   :tasks                  (indent 0 (build-tasks opts))
   :deps                   (dep-list 17 (dependencies opts))
   :requires               (indent 1 (build-requires opts))
   ;; :pre-build-steps        (indent 8 (pre-build-steps name opts))
   :build-steps            (indent 8 (build-steps name opts))
   :run-steps              (indent 8 (run-steps name opts))
   :production-task-opts   (indent 22 (production-task-opts opts))
   :development-task-opts  (indent 22 (development-task-opts opts))
   :index-html-script-tags (indent 4 (index-html-script-tags opts))
   :index-html-head-tags   (indent 4 (index-html-head-tags opts))})

(defn warn-on-exclusive-opts!
  "Some options can't be used together w/o added complexity."
  [opts]
  (let [x (juxt om? om-next? reagent?)]
    (when-not (>= 1 (count (keep identity (x opts))))
      (main/warn "Please specify only +om, +om-next or +reagent, not multiple.")
      (main/exit))))

(defn render-boot-properties []
  (let [{:keys [exit out err]} (sh/sh "boot" "-V" :env {:BOOT_CLOJURE_VERSION "1.8.0"})]
    (if (pos? exit)
      (println "WARNING: unable to produce boot.properties file.")
      out)))

(defn mute-implicit-target-warning [boot-props]
  (let [line-sep (System/getProperty "line.separator")]
    (string/join line-sep 
                 (conj (string/split (render-boot-properties) 
                                     (re-pattern line-sep)) 
                       (str "BOOT_EMIT_TARGET=no" line-sep)))))

(defn tenzing
  "Main function to generate new tenzing project."
  [name & opts]
  (let [data     (template-data name opts)
        app-cljs "src/cljs/{{sanitized}}/app.cljs"]
    (main/info "Generating fresh 'lein new' tenzing project.")
    (warn-on-exclusive-opts! opts)
    (apply (partial ->files data)
           (remove nil?
                   (vector (if (garden? opts)  ["src/clj/{{sanitized}}/styles.clj" (render "styles.clj" data)])
                           (if (sass? opts)    ["sass/css/sass.scss" (render "sass.scss" data)])

                           (if (test? opts)    ["test/cljs/{{sanitized}}/app_test.cljs" (render "app_test.cljs" data)])

                           (if (less? opts)    ["less/less.main.less" (render "less.main.less" data)])

                           (cond (reagent? opts) [app-cljs (render "reagent-app.cljs" data)]
                                 (om? opts)      [app-cljs (render "om-app.cljs" data)]
                                 (om-next? opts) [app-cljs (render "om-next-app.cljs" data)]
                                 (rum? opts)     [app-cljs (render "rum.cljs" data)]
                                 :none           [app-cljs (render "app.cljs" data)])

                           ["resources/js/app.cljs.edn" (render "app.cljs.edn" data)]
                           ["resources/index.html" (render "index.html" data)]

                           (when-let [boot-props (render-boot-properties)]
                             ["boot.properties" (mute-implicit-target-warning boot-props)])

                           ["build.boot" (render "build.boot" data)]
                           [".gitignore" (render "gitignore" data)])))))
