(defproject cljrpgengine "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.5.0"]
                 [quil "4.3.1563"]
                 [clojure.java-time "1.4.2"]]
  :main ^:skip-aot cljrpgengine.core
  :target-path "target/%s"
  :plugins [[lein-pprint "1.3.2"]
            [dev.weavejester/lein-cljfmt "0.12.0"]
            [lein-cloverage "1.2.2"]]
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
