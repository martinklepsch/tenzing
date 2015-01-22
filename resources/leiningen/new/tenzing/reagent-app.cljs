(ns {{name}}.app
  (:require [reagent.core :as reagent :refer [atom]]))

(defn some-component []
  [:div
   [:h3 "I am a component!"]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red"]
    " text."]])

(defn calling-component []
  [:div "Parent component"
   [some-component]])

(defn init []
  (reagent/render-component [calling-component]
                            (.getElementById js/document "container")))
