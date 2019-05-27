(ns wise.ajax
  (:require [ajax.core :refer [GET POST DELETE]]
            [reagent.cookies :as cookies]
            [clojure.string :as cstr]))

#_(def meta! (atom {:service-uris   ""
                    :save-handler   (fn [])
                    :delete-handler (fn [])
                    :error-handler  (fn [])}))

(def ^:private meta! (atom {}))

(defn- merge-url [url]
  (let [uris (:service-uris @meta!)]
    (if (string? uris)
      (str uris url)
      (let [server (cstr/split url #"/" 2)
            server-name (-> server first (str "/"))
            server-url (second server)
            head-uri (some #(when (cstr/ends-with? % server-name) %) uris)]
        (str head-uri server-url)))))

(defn- headers []
  (let [token (cookies/get :token)]
    (when token
      {:authorization (str "Token " token)})))

(defn- default-opts
  ([f] {:handler       f
        :error-handler (:error-handler @meta!)})
  ([f1 f2] {:handler       f1
            :error-handler #(do ((:error-handler @meta!) %) (f2))}))

(defn- resp-json-opts [f]
  (merge (default-opts f) {:response-format :json :keywords? true}))

(defn- params-opts
  ([params f]
   (merge (default-opts f) {:params params}))
  ([params f1 f2]
   (merge (default-opts f1 f2) {:params params})))

(defn- headers-opts
  ([f] (merge (default-opts f) {:headers (headers)}))
  ([f1 f2] (merge (default-opts f1 f2) {:headers (headers)})))

(defn- headers-params-opts
  [params f] (merge (headers-opts f) {:params params}))

(defn- headers-opts-res-json
  ([f] (merge (headers-opts f) {:response-format :json :keywords? true}))
  ([params f] (merge (headers-opts-res-json f) {:params params})))

(defn- reset-ajax [uri f1 f2 f3]
  (reset! meta! {:service-uris       uri
                     :save-handler   f1
                     :delete-handler f2
                     :error-handler  f3}))

(defn init!
  ([uri]
   (when (empty? @meta!)
     (reset-ajax uri #(js/alert "保存成功！") #(js/alert "删除成功！") #(js/alert "服务器错误！"))))
  ([uri error-fn]
   (when (empty? @meta!)
     (reset-ajax uri #(js/alert "保存成功！") #(js/alert "删除成功！") error-fn)))
  ([uri save-fn delete-fn error-fn]
   (when (empty? @meta!)
     (reset-ajax uri save-fn delete-fn error-fn))))

(defn get!
  ([url f]
   (GET (merge-url url) (headers-opts-res-json f)))
  ([url params f]
   (GET (merge-url url) (headers-opts-res-json params f))))

(defn post!
  ([url f]
   (POST (merge-url url) (headers-opts-res-json f)))
  ([url params f]
   (POST (merge-url url) (headers-opts-res-json params f))))

(defn delete!
  ([url]
   (DELETE (merge-url url) (headers-opts (:delete-handler @meta!))))
  ([url f]
   (DELETE (merge-url url) (headers-opts #(do (:delete-handler @meta!) (f %))))))

(defn save!
  ([url params]
   (POST (merge-url url) (headers-params-opts params (:save-handler @meta!))))
  ([url params f]
   (POST (merge-url url) (headers-params-opts params #(do (:save-handler @meta!) (f %))))))

(defn auth-login [url params f1 f2]
  (POST (merge-url url) (params-opts params f1 f2)))

(defn auth-refresh [url f1 f2]
  (POST (merge-url url) (headers-opts f1 f2)))
