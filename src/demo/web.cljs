(ns demo.web
  (:require [reagent.core :as reagent]
            [wise.ajax :as ajax]))

(defn svc []
  (fn [] ()
    (ajax/post! "abc/bcd" #(*print-fn* %))
    ;(ajax/get! "abc" #(*print-fn* %))
    (*print-err-fn* "1")
    (*print-fn* (reagent/atom {:a 1}))
    [:div "abc"]))

(defn home []
  (fn []
    (ajax/init! "http://localhost:3000/")
    [:div "asdasd" [svc]]))

(defn mount-root []
  (reagent/render [home] (.getElementById js/document "app")))

(mount-root)
