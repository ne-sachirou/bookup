(defproject bookup/bookup "0.1.0-SNAPSHOT"
  :description "List up my books"
  :url "https://github.com/ne-sachirou/bookup"
  :license {:name "GPL-3.0"
            :url "https://www.gnu.org/licenses/gpl-3.0.en.html"}

  :global-vars {clojure.core/*warn-on-reflection* true}

  :source-paths ["src/clojure" "src"]
  :java-source-paths ["src/java"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :plugins [[lein-cljfmt "0.5.7"]
            [lein-droid "0.4.6"]]

  :dependencies [[com.google.zxing/android-integration "3.3.0"]
                 [funcool/cats "2.1.0"]
                 [neko/neko "4.0.0-alpha5"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure-android/clojure "1.7.0-r4"]]

  :profiles {:default [:dev]

             :dev
             [:android-common :android-user
              {:dependencies [[org.clojure/tools.nrepl "0.2.10"]]
               :target-path "target/debug"
               :android {:aot :all-with-unused
                         :aot-exclude-ns [#"^cats.labs\..+"]
                         :manifest-options {:app-name "BookUp (debug)"}
                         ;; Uncomment to be able install debug and release side-by-side.
                         ;; :rename-manifest-package "jp.c4se.bookup.debug"
                         }}]
             :release
             [:android-common
              {:target-path "target/release"
               :android
               {;; :keystore-path "/home/user/.android/private.keystore"
                ;; :key-alias "mykeyalias"
                ;; :sigalg "MD5withRSA"

                :use-debug-keystore true
                :ignore-log-priority [:debug :verbose]
                :aot :all
                :build-type :release}}]

             :lean
             [:release
              {:dependencies ^:replace [[org.skummet/clojure "1.7.0-r2"]
                                        [neko/neko "4.0.0-alpha5"]]
               :exclusions [[org.clojure/clojure]
                            [org.clojure-android/clojure]]
               :jvm-opts ["-Dclojure.compile.ignore-lean-classes=true"]
               :android {:lean-compile true
                         :proguard-execute true
                         :proguard-conf-path "build/proguard-minify.cfg"}}]}

  :android {;; Specify the path to the Android SDK directory.
            :sdk-path "/usr/local/share/android-sdk/"

            ;; Increase this value if dexer fails with OutOfMemoryException.
            :dex-opts ["-JXmx4096M" "--incremental"]

            :target-version "24"
            :aot-exclude-ns ["clojure.parallel" "clojure.core.reducers"
                             "cider.nrepl" "cider-nrepl.plugin"
                             "cider.nrepl.middleware.util.java.parser"
                             #"cljs-tooling\..+"]}

  :android-user {:dependencies [[cider/cider-nrepl "0.15.1"]]
                 :android {:aot-exclude-ns ["cider.nrepl.middleware.util.java.parser"
                                            "cider.nrepl" "cider-nrepl.plugin"]}})
