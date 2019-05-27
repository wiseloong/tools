(ns wise.utils
  (:require [clj-time.core :as ct]
            [clj-time.format :as ctf]
            [buddy.core.hash :as hash]
            [buddy.core.codecs :as codecs]
            [cheshire.core :as cjson]
            [clojure.string :as cstr])
  (:import (org.joda.time DateTime)))

(defn DateTime? "判断是否是时间格式"
  [d] (instance? DateTime d))

(def PRC "+8区"
  (ct/time-zone-for-id "PRC"))

(def date-formatters "时间格式类型，可通过key获取对应的格式"
  {:date-time  "yyyy-MM-dd HH:mm:ss"
   :date       "yyyy-MM-dd"
   :date-tight "yyyyMMdd"
   :date-ym    "yyyyMM"
   :file-tail  "_yyyyMMddHHmmss"})

(defn- date-format "默认+8区，yyyy-MM-dd，时间格式"
  ([] (date-format :date))
  ([k] (ctf/formatter (k date-formatters) PRC)))

(defn date-string "把时间更新为字符串格式，错误返回nil"
  ([date] (date-string date :date))
  ([date format]
   (when (DateTime? date)
     (ctf/unparse (date-format format) date))))

(defn string-date "把字符串更新为时间格式，错误返回nil"
  ([s] (string-date s :date))
  ([s format]
   (when s
     (try (ctf/parse (date-format format) s)
          (catch Exception _)))))

(defn subs-string-date "截取字符串后转为时间，根据时间格式，比如截取2000-01-01 10:10:10为2000-01-01再转为时间"
  ([s] (subs-string-date s :date))
  ([s format]
   (let [c (count (format date-formatters))]
     (when (<= c (count s))
       (string-date (subs s 0 c) format)))))

(defn unparse "格式时间"
  ([dt] (unparse dt :date))
  ([dt format]
   (if (DateTime? dt)
     (ctf/unparse (date-format format) dt)
     dt)))

(defn now-string "当前时间-字符串"
  ([] (now-string :date-time))
  ([format]
   (date-string (ct/now) format)))

(defn now "当前时间，+8区"
  [] (ct/to-time-zone (ct/now) PRC))

(defn map-date-string "把map里所有的date转换为string格式"
  ([m] (map-date-string m :date))
  ([m format]
   (letfn [(f [[k v]] [k (unparse v format)])]
     (->> (map f m)
          (into {})))))

(defn map-string-date "把map里的某个key对应的字符串更新为时间格式"
  ([m key] (map-string-date m key :date))
  ([m key format]
   (assoc m key (ctf/parse (date-format format) (key m)))))

(defn maps-date-string "把map集合里所有的date转换为string格式"
  ([coll] (map map-date-string coll))
  ([coll format]
   (map #(map-date-string % format) coll)))

(defn merge-insert "给集合添加创建时间，修改时间，有效性，适用于数据库新增数据"
  [m]
  (let [now (ct/now)]
    (merge m {:create_date now :modify_date now :is_valid 1})))

(defn merge-update "给集合添加修改时间，适用于数据库修改数据"
  [m]
  (merge m {:modify_date (ct/now)}))

(defn for-map "修改map里的每个元素的值，对每个v执行f函数得到新的v"
  [m f]
  (letfn [(l [[k v]] [k (f v)])]
    (->> (map l m)
         (into {}))))

(defn page-size "转换page和size为数据库需要的参数
  {:page 1 :size 10} 变成
  {:start 0 :end :10 :size 10}
  mysql 分页 limit :start, :size
  oracle 分页 select * from (select row_.*, rownum rn from (xxx) row_ where rownum <= :end) where rn > :start"
  [params]
  (let [page (:page params)
        size (:size params)]
    (if (and page size)
      (let [start (* size (- page 1))
            end (* size page)]
        (dissoc params :page)
        (merge params {:start start :end end}))
      params)))

(defn parse-int "字符串转int"
  [s]
  (if s
    (Integer/parseInt (re-find #"-?\d+" s))
    0))

(defn md5 [s]
  (-> s hash/md5 codecs/bytes->hex))

(defn differ-map "以m1为基准，获取两个map的不同值，组成[{:fields key, :new v1, :old v2}]"
  [m1 m2]
  (letfn [(merge-data [[k v]] {:fields k :new v :old (get m2 k)})
          (differ [{:keys [new old]}] (not (= new old)))]
    (->> m1
         (map merge-data)
         (filter differ))))

(defn collection-map-key
  "把map集合里，k的值v相同的map合并，以v值作为新key，结果为{:v [m1,m2] :v2 []}
  k的值需要为字符串，不能是数字等"
  ([coll k] (collection-map-key coll k {}))
  ([coll k r]
   (let [one (first coll)]
     (if-not (nil? one)
       (let [k1 (keyword (k one))
             v1 (conj (k1 r) one)]
         (collection-map-key (next coll) k (assoc r k1 v1)))
       r))))

(defn any-empty? "判断是否为空，遇到数字类型不报错"
  [a] (or (nil? a) (and (string? a) (cstr/blank? a))))

(defn join-comma "去除空元素，把集合合并成字符串,用','分割"
  [coll] (let [coll (remove any-empty? coll)]
           (cstr/join "," coll)))

(defn split-comma "把用','分割的字符串转为集合，排除空元素"
  [s] (when-not (any-empty? s)
        (let [c (cstr/split s #",")]
          (remove any-empty? c))))

(defn parse-string "字符串转json，转成keyword形式 {:id id}"
  [str] (cjson/parse-string str true))

(defn some-one
  "循环map集合，根据方法获取某个map
  或者获取k=v的map
  或者获取k1=v1的map的k2的值"
  ([coll f?] (some #(when (f? %) %) coll))
  ([coll k v]
   (some #(when (= v (k %)) %) coll))
  ([coll k1 v1 k2]
   (k2 (some-one coll k1 v1))))

(defn birthday-age "根据身份证号码获取出生日期和性别"
  [id-card]
  (let [count (count id-card)]
    (cond
      (= 15 count)
      (let [bs (str "19" (subs id-card 6 12))
            bd (string-date bs :date-tight)]
        {:birthday bd})
      (= 18 count)
      (let [bs (subs id-card 6 14)
            bd (string-date bs :date-tight)]
        {:birthday bd}))))

(defn keywordize "适用于转换数据库表名，字段名为clojure形式"
  [s]
  (-> (cstr/lower-case s)
      (cstr/replace "_" "-")
      (keyword)))
