(ns user
  (:use [clojure.repl]
        [devtool.server]
        [devtool.web])
  (:require [demo.handler :as handler]))

(defmethod server "init" [_] handler/init)

(defmethod server "destroy" [_] handler/destroy)

(defmethod server "handler" [_] #'handler/app)
