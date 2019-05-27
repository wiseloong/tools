(ns wise.utils
  (:require [clojure.string :as cstr]
            [goog.string :as gstr]
            [goog.uri.utils :as guu]
            [cljs-time.core :as ct]
            [cljs-time.format :as ctf]))

(def date-formatters "时间格式类型，可通过key获取对应的格式"
  {:date-time  "yyyy-MM-dd HH:mm:ss"
   :date       "yyyy-MM-dd"
   :date-tight "yyyyMMdd"
   :date-ym    "yyyyMM"
   :file-tail  "_yyyyMMddHHmmss"})

(defn- date-format "默认yyyy-MM-dd，时间格式"
  ([] (date-format :date))
  ([k] (ctf/formatter (k date-formatters))))

(defn now-string "当前时间-字符串"
  ([] (now-string :date-time))
  ([format]
   (ctf/unparse (date-format format) (ct/time-now))))

(defn differ-minute "与当前时间相差多少分钟"
  [date]
  (let [now (js/Date.now)
        differ (- now date)]
    (/ differ 60000)))

(defn href "跳转url，可用于下载"
  [url] (set! (.-href js/location) url))

(defn select-data "转换map集合数据为下拉框数据
  [{:id 1 :name \"a\"}{:id 2 :name \"b\"}]
  [1 \"a\" 2 \"b\"]"
  [coll]
  (letfn [(f [{:keys [id name]}] [id name])]
    (->> coll
         (map f)
         (reduce into))))

(defn url-decode [s]
  (gstr/urlDecode s))

(defn url-encode [s]
  (gstr/urlEncode s))

(defn url-param "获取url里的参数k的值"
  ([k] (url-param (.-href js/location) k))
  ([url k] (guu/getParamValue url k)))

(defn url-params "根据url地址获取?后面的参数"
  [uri]
  (let [s (-> (cstr/split uri #"\?" 2) second)]
    (when-not (cstr/blank? s)
      (letfn [(key-fn [[k v]] {(keyword k) v})
              (param-map [s]
                (-> (cstr/split s #"=" 2)
                    key-fn))
              (merge-list [m e]
                (if (sequential? m)
                  (conj m e)
                  [m e]))]
        (as-> (gstr/urlDecode s) m
              (cstr/split m #"&")
              (map param-map m)
              (apply merge-with merge-list m))))))

(defn params-url "根据参数拼接得到?后面的url地址"
  [params]
  (let [enc (fn [a b] (str (if (keyword? a) (name a) a) "=" (gstr/urlEncode (str b))))
        join (fn [v] (apply str (interpose "&" v)))]
    (join
      (map (fn [[k v]]
             (if (sequential? v)
               (join (map enc (repeat k) v))
               (enc k v)))
           params))))
