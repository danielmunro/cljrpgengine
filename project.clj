(defproject cljrpgengine "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.5.0"]
                 [clojure.java-time "1.4.2"]
                 [org.flatland/ordered "1.15.12"]
                 [com.badlogicgames.gdx/gdx "1.13.0"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.13.0"]
                 [com.badlogicgames.gdx/gdx-box2d "1.13.0"]
                 [com.badlogicgames.gdx/gdx-box2d-platform "1.13.0"
                  :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-bullet "1.13.0"]
                 [com.badlogicgames.gdx/gdx-bullet-platform "1.13.0"
                  :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-platform "1.13.0"
                  :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-freetype "1.13.0"]
                 [com.badlogicgames.gdx/gdx-freetype-platform "1.13.0"
                  :classifier "natives-desktop"]]
  :main cljrpgengine.core
  :target-path "target/%s"
  :plugins [[lein-pprint "1.3.2"]
            [dev.weavejester/lein-cljfmt "0.12.0"]
            [lein-cloverage "1.2.2"]]
  :aot [cljrpgengine.game])
