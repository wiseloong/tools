(ns wise.middleware.http
  (:require [ring.util.http-response :refer [ok no-content]]))

;; 列表和单个结果不适用，还是要加上ok返回的：(GET "/def" [] (ok "xxx")) (GET "/efg" [] (ok [1 2 3]))
;; 注意当有这2个接口时：(GET "/abc/bcd" [] {:id 1})  (GET "/abc/:id" [] :path-params [id :- s/Int] (ok {:id id}))
;; 访问/abc/bcd时，ring匹配的是/abc/:id这个，这样的情况也不适用，第一个接口也要加上(ok {:id 1})
(defn response-mw
  "可以省去写返回ok，或no-content，直接返回结果，只支持map类结果
  1. (GET \"/abc\" [])  2. (GET \"/bcd\" [] {:id 2 :ids [1 2 4 5]})"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if response
        (let [body (:body response)]
          (if (or (nil? body) (= "" body))
            (let [data (dissoc response :status :headers :body)]
              (if (empty? data)
                (no-content)
                (ok data)))
            response))
        (no-content)))))
