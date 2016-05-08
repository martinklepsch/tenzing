(ns {{name}}.app
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(def app-state (atom {:greeting "Hello world!"}))

(defui App
  Object
  (render [this] 
    (let [{:keys [greeting]} (om/props this)]
      (dom/h1 nil greeting))))

(def reconciler
  (om/reconciler {:state app-state}))

(defn init []
  (om/add-root! reconciler
                App 
                (gdom/getElement "container")))

