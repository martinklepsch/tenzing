(ns leiningen.new.tenzing
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files]]
            [leiningen.core.main :as main]))

(def render (renderer "tenzing"))

(defn reagent? [opts]
  (some #{"+reagent"} opts))

(defn om? [opts]
  (some #{"+om"} opts))

(defn sass? [opts]
  (some #{"+sass"} opts))

(defn sass? [opts]
  (some #{"+garden"} opts))

(defn tenzing
  "FIXME: write documentation"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (main/info "Generating fresh 'lein new' tenzing project.")
    (->files data
             ;["src/{{sanitized}}/foo.clj" (render "foo.clj" data)]
             ["resources/public/index.html" (render "index.html" data)]
             ["src/cljs/{{sanitized}}/app.cljs" (render "app.cljs" data)]
             ["build.boot" (render "build.boot" data)])))
