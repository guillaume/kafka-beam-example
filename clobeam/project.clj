(def ^:private apache-beam-version "2.1.0")

(defproject clobeam "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.apache.beam/beam-sdks-java-core ~apache-beam-version]
                 [org.apache.beam/beam-runners-direct-java ~apache-beam-version]
                 [org.apache.beam/beam-runners-google-cloud-dataflow-java ~apache-beam-version]
                 [org.apache.beam/beam-runners-core-java ~apache-beam-version]
                 [org.apache.beam/beam-sdks-java-io-kafka ~apache-beam-version]]
  :java-source-paths ["src-java"]

  :repl-options {:init-ns clobeam.core})
