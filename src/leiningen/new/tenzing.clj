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

; ---------------------------------------------------------------
; Options - currently: divshot, reagent, om, sass, garden
; ---------------------------------------------------------------

(defn divshot? [opts]
  (some #{"+divshot"} opts))

(defn reagent? [opts]
  (some #{"+reagent"} opts))

(defn om? [opts]
  (some #{"+om"} opts))

(defn sass? [opts]
  (some #{"+sass"} opts))

(defn garden? [opts]
  (some #{"+garden"} opts))

(defn less? [opts]
  (some #{"+less"} opts))

(defn test? [opts]
  (some #{"+test"} opts))

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
          (test?    opts) (conj "crisptrutski/boot-cljs-test \"0.1.0-SNAPSHOT\" :scope \"test\"")
          (om?      opts) (conj "org.omcljs/om \"0.8.6\"")
          (reagent? opts) (conj "reagent \"0.5.0\"")
          (garden?  opts) (conj "org.martinklepsch/boot-garden \"1.2.5-3\" :scope \"test\"")
          (sass?    opts) (conj "mathias/boot-sassc  \"0.1.1\" :scope \"test\"")
          (less?    opts) (conj "deraen/boot-less \"0.2.1\" :scope \"test\"")))

(defn build-requires [opts]
  (cond-> []
          (test?   opts) (conj "'[crisptrutski.boot-cljs-test :refer [test-cljs]]")
          (garden? opts) (conj "'[org.martinklepsch.boot-garden :refer [garden]]")
          (sass?   opts) (conj "'[mathias.boot-sassc  :refer [sass]]")
          (less?   opts) (conj "'[deraen.boot-less    :refer [less]]")))

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
  (cond-> []
          (test? opts) (conj test-tasks)))

;; (defn pre-build-steps [name opts]
;;   (cond-> []
;;           ))

(defn build-steps [name opts]
  (cond-> []
          (garden? opts) (conj (str "(garden :styles-var '" name ".styles/screen\n:output-to \"css/garden.css\")"))
          (sass?   opts) (conj (str "(sass :output-dir \"css\")"))
          (less?   opts) (conj (str "(less)"))
          (less?   opts) (conj (str "(sift   :move {#\"less.css\" \"css/less.css\" #\"less.main.css.map\" \"css/less.main.css.map\"})"))))

(defn production-task-opts [opts]
  (cond-> []
          (garden? opts) (conj (str "garden {:pretty-print false}"))
          (sass?   opts) (conj (str "sass   {:output-style \"compressed\"}"))
          (less?   opts) (conj (str "less   {:compression true}"))))

(defn development-task-opts [opts]
  (cond-> []
    (sass? opts) (conj (str "sass   {:line-numbers true
                                     :source-maps  true}"))
    (less? opts) (conj (str "less   {:source-map  true}"))))

(defn index-html-head-tags [opts]
  (letfn [(style-tag [href] (str "<link href=\"" href "\" rel=\"stylesheet\" type=\"text/css\" media=\"screen\">"))]
    (cond-> []
            (garden? opts) (conj (style-tag "css/garden.css"))
            (sass? opts)   (conj (style-tag "css/sass.css"))
            (less? opts)   (conj (style-tag "css/less.css")))))

(defn index-html-script-tags [opts]
  (letfn [(script-tag [src] (str "<script type=\"text/javascript\" src=\"" src "\"></script>"))]
    (cond-> []
            :finally (conj (script-tag "js/app.js")))))

(defn template-data [name opts]
  {:name                   name
   :sanitized              (name-to-path name)
   :source-paths           (source-paths opts)
   :tasks                  (indent 0 (build-tasks opts))
   :deps                   (dep-list 17 (dependencies opts))
   :requires               (indent 1 (build-requires opts))
   ;; :pre-build-steps        (indent 8 (pre-build-steps name opts))
   :build-steps            (indent 8 (build-steps name opts))
   :production-task-opts   (indent 22 (production-task-opts opts))
   :development-task-opts  (indent 22 (development-task-opts opts))
   :index-html-script-tags (indent 4 (index-html-script-tags opts))
   :index-html-head-tags   (indent 4 (index-html-head-tags opts))})

(defn warn-on-exclusive-opts!
  "Some options can't be used together w/o added complexity."
  [opts]
  (when (and (om? opts)
             (reagent? opts))
    (main/warn "Please specify only +om or +reagent, not both.")
    (main/exit)))

(defn render-boot-properties []
  (let [{:keys [exit out err]} (sh/sh "boot" "-V" :env {:BOOT_CLOJURE_VERSION "1.7.0"})]
    (if (pos? exit)
      (println "WARNING: unable to produce boot.properties file.")
      out)))

(def warn-sass-missing
  "Libsass was not detected on your machine. Please note that builds will fail until you configure a SASS compiler like sassc and place it in your PATH variable.")

(defn sass-missing? [opts]
  (and (sass? opts) (pos? (:exit (sh/sh "sassc" :in " ")))))

(def env-tests
  {sass? warn-sass-missing})

(defn warn-on-env-issues! [opts]
  (doseq [[fail? warning] env-tests]
    (when (fail? opts)
      (print "\n")
      (println warning)
      (print "\n"))))

(defn tenzing
  "Main function to generate new tenzing project."
  [name & opts]
  (let [data     (template-data name opts)
        app-cljs "src/cljs/{{sanitized}}/app.cljs"]
    (main/info "Generating fresh 'lein new' tenzing project.")
    (warn-on-exclusive-opts! opts)
    (warn-on-env-issues! opts)
    (apply (partial ->files data)
           (remove nil?
                   (vector (if (divshot? opts) ["divshot.json" (render "divshot.json" data)])
                           (if (garden? opts)  ["src/clj/{{sanitized}}/styles.clj" (render "styles.clj" data)])
                           (if (sass? opts)    ["sass/sass.scss" (render "sass.scss" data)])

                           (if (test? opts)    ["test/cljs/{{sanitized}}/app_test.cljs" (render "app_test.cljs" data)])

                           (if (less? opts)    ["less/less.main.less" (render "less.main.less" data)])

                           (cond (reagent? opts) [app-cljs (render "reagent-app.cljs" data)]
                                 (om? opts)      [app-cljs (render "om-app.cljs" data)]
                                 :none           [app-cljs (render "app.cljs" data)])

                           ["resources/js/app.cljs.edn" (render "app.cljs.edn" data)]
                           ["resources/index.html" (render "index.html" data)]

                           (when-let [boot-props (render-boot-properties)]
                             ["boot.properties" boot-props])

                           ["build.boot" (render "build.boot" data)]
                           [".gitignore" (render "gitignore" data)])))))
