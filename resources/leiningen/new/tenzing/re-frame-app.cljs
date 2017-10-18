(ns {{name}}.app
    (:require [reagent.core :as reagent :refer [atom]]
              [clairvoyant.core :refer-macros [trace-forms]]
              [re-frame-tracer.core :refer [tracer]]
              [re-frame.core :as re-frame]))

(defn setup! []
  ;; Cofx Registrations
  (trace-forms {:tracer (tracer :color "purple")}
    (re-frame/reg-cofx
     :thing
     (fn thing-cofx [cofx _]
       ))

    )

  ;; Fx Registrations
  (trace-forms {:tracer (tracer :color "gold")}
    (re-frame/reg-fx
     :thing
     (fn thing-fx [calls]
       ))

    )

  ;; Events Registrations
  (trace-forms {:tracer (tracer :color "green")}
    (re-frame/reg-event-db
     :init-db
     (fn init-db-event [db [_ error]]
       {:page-key ::main}))

    )

  ;; Subscriptions Registrations
  (trace-forms {:tracer (tracer :color "brown")}
    (re-frame/reg-sub
     :page-key
     (fn page-key-sub [app-db _]
       (:page-key app-db)))

    ))

(defn four-oh-four []
  [:div
   [:h1 "Sorry!"]
   "There's nothing to see here. Try checking out the "
   [:a {:href "/#"} "root"]
   "."])

(defn hello-world []
  [:h1 "Hello, Re-frame!"])

(defn main-panel [page-key]
  (case page-key
    ::main [hello-world]
    [four-oh-four]))

(defn main-panel-container []
  (let [page-key (re-frame/subscribe [:page-key])]
    [#'main-panel @page-key]))

(defn render-root []
  (if-let [node (.getElementById js/document "container")]
    (reagent/render-component [#'main-panel-container] node)))

(defn ^:export init []
  (re-frame.core/clear-subscription-cache!)
  (setup!)
  (re-frame/dispatch [:init-db])
  (render-root))
