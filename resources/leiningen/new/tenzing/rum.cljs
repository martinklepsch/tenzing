(ns {{name}}.app
    (:require [rum.core :as rum]))

(rum/defc label [text]
  [:div
   [:h1 "A label"]
   [:p text]])

(defn init []
  (rum/mount (label) (. js/document (getElementById "container"))))
