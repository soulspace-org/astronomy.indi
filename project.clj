(defproject org.soulspace.clj/astronomy.indi "0.1.0-SNAPSHOT"
  :description "Clojure INDI Library"
  :url "https://github.com/soulspace-org/astronomy.indi"

  ; use deps.edn dependencies
  ;:plugins [[lein-tools-deps "0.4.5"]]
  ;:middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  ;:lein-tools-deps/config {:config-files [:install :user :project]}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.5.640"]
                 [org.clojure/spec.alpha "0.3.214"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.soulspace.clj/clj.java "0.9.0"]
                 [org.soulspace.clj/xml.dsl "0.5.1"]]

  :profiles {:dev {:dependencies [[org.clojure/test.check "1.1.0"]]}}
  :repl-options {:init-ns org.soulspace.astronomy.indi.protocol}

  :test-paths ["test"]
  :scm {:name "git" :url "https://github.com/soulspace-org/astronomy.indi"}
  :deploy-repositories [["clojars"  {:sign-releases false :url "https://clojars.org/repo"}]])
