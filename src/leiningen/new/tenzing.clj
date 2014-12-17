(ns leiningen.new.tenzing
  (:require [leiningen.new.templates :as t :refer [name-to-path ->files sanitize slurp-resource]]
            [leiningen.core.main :as main]
            [clojure.string :as string]
            [clojure.java.io :as io]))


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

(defn source-paths [opts]
  (cond-> #{"src/cljs"}
          (garden? opts) (conj "src/clj")
          (sass? opts)   (conj "sass")))

(defn dependencies [opts]
  (cond-> ["pandeiro/boot-http \"0.3.0\""]
          (om?      opts) (conj "om \"0.7.3\"")
          (reagent? opts) (conj "reagent \"0.4.3\"")
          (garden?  opts) (conj "boot-garden \"1.2.5-1\"")
          (sass?    opts) (conj "boot-sassc  \"0.1.0\"")
          (or (reagent? opts)
              (om?      opts)) (conj "cljsjs/react \"0.11.2\"")))

(defn build-requires [opts]
  (cond-> []
          (garden? opts) (conj "'[boot-garden.core :refer [garden]]")
          (sass?   opts) (conj "'[boot-sassc.core  :refer [sass]]") ))

(defn build-steps [name opts]
  (cond-> []
          (garden? opts) (conj (str "(garden :styles-var '" name ".styles/screen\n:output-to \"public/css/garden.css\")"))
          (sass?   opts) (conj (str "(sass :output-to \"public/css/sass.css\")"))))

(defn production-task-opts [opts]
  (cond-> []
          (garden? opts) (conj (str "garden {:pretty-print false}"))
          (sass?   opts) (conj (str "sass   {:output-style \"compressed\"}"))))

(defn development-task-opts [opts]
  (cond-> []
          (sass? opts) (conj (str "sass   {:line-numbers true
                                           :source-maps  true}"))))

(defn index-html-head-tags [opts]
  (let [style-tag #(str "<link href=\"" % "\" rel=\"stylesheet\" type=\"text/css\" media=\"screen\">")]
    (cond-> []
            (garden? opts) (conj (style-tag "css/garden.css"))
            (sass?   opts) (conj (style-tag "css/sass.css")))))

(defn template-data [name opts]
  {:name name
   :sanitized (name-to-path name)
   :source-paths (source-paths opts)
   :deps (dep-list 17 (dependencies opts))
   :requires (indent 1 (build-requires opts))
   :build-steps (indent 8 (build-steps name opts))
   :production-task-opts (indent 22 (production-task-opts opts))
   :development-task-opts (indent 22 (development-task-opts opts))
   :index-html-head-tags (indent 4 (index-html-head-tags opts))})

(defn warn-on-exclusive-opts! [opts]
  (when (and (om? opts)
             (reagent? opts))
    (main/warn "Please specify only +om or +reagent, not both.")
    (main/exit)))

(defn tenzing
  "FIXME: write documentation"
  [name & opts]
  (let [data     (template-data name opts)
        app-cljs "src/cljs/{{sanitized}}/app.cljs"]
    (main/info "Generating fresh 'lein new' tenzing project.")
    (warn-on-exclusive-opts! opts)
    (apply (partial ->files data)
           (remove nil?
                   (vector (if (divshot? opts) ["divshot.json" (render "divshot.json" data)])
                           (if (garden? opts)  ["src/clj/{{sanitized}}/styles.clj" (render "styles.clj" data)])
                           (if (sass? opts)    ["sass/styles.sass" (render "styles.sass" data)])
                           (if (reagent? opts) [app-cljs (render "reagent-app.cljs" data)])
                           (if (om? opts)      [app-cljs (render "om-app.cljs" data)])

                           ["resources/public/index.html" (render "index.html" data)]
                           ["build.boot" (render "build.boot" data)])))))
