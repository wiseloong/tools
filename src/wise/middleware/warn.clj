(ns wise.middleware.warn
  (:require [ring.util.http-response :refer [ok bad-request]]
            [mount.core :refer [defstate]]
            [wise.config :refer [env]]
            [clojure.tools.logging :as log])
  (:import (clojure.lang ExceptionInfo)))

;[cprop.core :refer [load-config]]
#_(defonce ^:private app-name
           (-> (load-config) :application :name))

(defstate app-name
          :start (-> env :application :name))

(defn- is-warn? [e]
  (and (-> e class (= ExceptionInfo)) (-> e .getData :wiseloong)))

(defn- find-trace [trace]
  (let [start-app-class (fn [c] (re-find (re-pattern (str "^*" app-name "\\.")) c))
        filter-trace (fn [e] (let [class (.getClassName e)]
                               (start-app-class class)))]
    (filter filter-trace trace)))

(defn- log-warn [e request]
  (let [user (-> request :identity :user)
        request-url (:uri request)
        request-method (:request-method request)
        params (:params request)
        error-msg (.getMessage e)
        data (dissoc (.getData e) :wiseloong)
        trace (find-trace (.getStackTrace e))]
    (log/warn "告警信息 =" error-msg "告警数据 =" data "用户ID =" (:id user) "用户编码 =" (:code user)
              "用户名 =" (:name user) "路径 =" request-url "方法 =" request-method "参数=" params)
    (mapv #(log/warn "\tat" (.toString %)) trace)
    (if (:no-bad data)
      (ok {:success false :msg error-msg :data data})
      (bad-request {:msg error-msg :data data}))))

(defn warn-mw [handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable e
        (if (is-warn? e)
          (log-warn e request)
          (throw e))))))

(defn throw-warn
  ([msg] (throw-warn msg nil))
  ([msg options]
   (throw (ex-info (str msg) (merge options {:wiseloong true})))))

(defn return-warn
  ([warn? msg]
   (when warn? (throw-warn msg)))
  ([warn? msg options]
   (when warn? (throw-warn msg options))))
