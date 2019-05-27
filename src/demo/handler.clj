(ns demo.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [wise.middleware.warn :refer [warn-mw return-warn]]
            [wise.middleware.cors :refer [cors-mw]]
            [mount.core :as mount]))

(defn init [] (doseq [component (:started (mount/start))]
                (println component "started")))

(defn destroy [] (doseq [component (:stopped (mount/stop))]
                   (println component "stopped")))

(def app
  (api
    :middleware [cors-mw warn-mw]
    (context "/abc" []
      :tags ["撒大"]

      (GET "/" []
        :summary "导入"
        (ok (let [a 123]
              (return-warn true "asdasad")
              {:id "asd"})))

      (POST "/bcd" []
        :summary "导入"
        (ok {:id "asd"}))

      )))
