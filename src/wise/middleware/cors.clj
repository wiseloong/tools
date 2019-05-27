(ns wise.middleware.cors
  (:require [mount.core :refer [defstate]]
            [ring.middleware.cors :refer [handle-cors add-access-control]]
            [wise.config :refer [env]]))

;[cprop.core :refer [load-config]]
;[ring.middleware.cors :refer [wrap-cors]]
#_(defonce ^:private cors-origins
           (-> (load-config) :rest-api :access-control-allow-origin))
#_(defn cors-mw [handler]
    (wrap-cors handler :access-control-allow-origin (map re-pattern cors-origins)
               :access-control-allow-methods [:get :put :post :delete]))

(defstate allow-origins
          :start (let [origins (-> env :rest-api :access-control-allow-origin)]
                   (map re-pattern origins)))

(defn cors-mw
  [handler]
  (fn [request]
    (let [access-control {:access-control-allow-origin  allow-origins
                          :access-control-allow-methods #{:get :post :delete :put}}]
      (handle-cors handler request access-control add-access-control))))
