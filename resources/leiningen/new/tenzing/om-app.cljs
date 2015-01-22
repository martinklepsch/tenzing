(ns {{name}}.app
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn widget [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/h1 nil (:text data)))))

(defn init []
  (om/root widget {:text "Hello world!"}
           {:target (. js/document (getElementById "container"))}))
