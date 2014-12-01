(ns {{name}}.styles
  (:require [garden.def :refer [defrule defstyles]]
            [garden.stylesheet :refer [rule]]))

(defstyles screen
  (let [body (rule :body)]
    (body
     {:font-family "Helvetica Neue"
      :font-size   "16px"
      :line-height 1.5})))
