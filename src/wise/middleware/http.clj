(ns wise.middleware.http
  (:require [ring.util.http-response :refer [ok no-content]]
            [clojure.string :as cstr]))

;; 注意当有这2个接口时：(GET "/abc/bcd" [] nil)  (GET "/abc/:id" [] :path-params [id :- s/Int] (ok {:id id}))
;; 访问/abc/bcd时，ring匹配的是/abc/:id这个，这样的情况也不适用，第一个接口也要加上(ok nil)
(defn response-mw
  "可以省去写返回ok，或no-content，直接返回结果
  1. (GET \"/abc\" [] \"abc\")  2. (GET \"/bcd\" [] {:id 2 :ids [1 2 4 5]})
  3. (GET \"/def\" [] (list 1 2 3))"
  [handler]
  (fn [request]
    (if-let [{:keys [status headers body] :as response} (handler request)]
      (if (= 200 status)
        (if body
          (if (= "" body)
            (let [data (dissoc response :status :headers :body)]
              (if (empty? data)
                (no-content)
                (ok data)))
            (if headers
              (if-let [content-type (headers "Content-Type")]
                (if (cstr/includes? content-type "text/html")
                  (ok body)
                  response)
                response)
              response))
          (no-content))
        response)
      (no-content))))
