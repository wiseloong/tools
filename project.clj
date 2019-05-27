(defproject wiseloong/tools "0.1.0-SNAPSHOT"
  :description "wiseloong-基础工具"
  :url "www.wiseloong.com"
  :license {:name "wiseloong"}

  :dependencies [;; clj
                 [clj-time "0.15.1"]
                 [buddy "2.0.0"]
                 [cheshire "5.8.1"]
                 [cprop "0.1.13"]
                 [mount "0.1.16"]
                 ;; cljs
                 [reagent "0.8.1"]
                 [reagent-utils "0.3.2"]
                 [cljs-ajax "0.7.5"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]]

  :jar-exclusions [#"(?:^|\/)demo\/"]

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src"]
                        :figwheel     true
                        :compiler     {:main       demo.web
                                       :asset-path "/out"
                                       :output-to  "target/cljsbuild/public/app.js"
                                       :preloads   [devtool.web]}}]}

  :profiles {:dev {:dependencies   [[org.clojure/clojure "1.9.0"]
                                    [org.clojure/clojurescript "1.10.439"]
                                    [metosin/compojure-api "2.0.0-alpha25"]
                                    [org.clojure/tools.logging "0.4.1"]
                                    [wiseloong/devtool "1.1.0"]]
                   :source-paths   ["dev/clj"]
                   :resource-paths ["dev/resources" "target/cljsbuild"]
                   :repl-options   {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
