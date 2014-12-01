(ns leiningen.new.tenzing
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files]]
            [leiningen.core.main :as main]))

(def render (renderer "tenzing"))

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

(defn tenzing
  "FIXME: write documentation"
  [name & opts]
  (let [data     {:name name
                  :sanitized (name-to-path name)}
        app-cljs "src/cljs/{{sanitized}}/app.cljs"]
    (main/info "Generating fresh 'lein new' tenzing project.")
    (when (and (om? opts) (reagent? opts))
      (main/warn "Please specify only +om or +reagent, not both.")
      (main/exit))
    (->files data

             (if (divshot? opts) ["divshot.json" (render "divshot.json" data)])
             ;(if (om? opts) ["" (render "divshot.json" data)])

             ["resources/public/index.html" (render "index.html" data)]
             ["build.boot" (render "build.boot" data)]
             [app-cljs (render "app.cljs" data)])))
