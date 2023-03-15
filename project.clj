(defproject org.soulspace.clj/astronomy.indi "0.1.0-SNAPSHOT"
  :description "Clojure INDI Library"
  :url "http://example.com/FIXME"

  ; use deps.edn dependencies
  :plugins [[lein-tools-deps "0.4.5"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]}

  :profiles {:dev {:dependencies [[org.clojure/test.check "1.1.0"]]}}
  :repl-options {:init-ns indi4clj.protocol})
