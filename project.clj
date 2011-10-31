(defproject mandel"1.0.0-SNAPSHOT"
  :description "A Mandelbrot web app"

  :cloudbees-app-id "sandoz/mandel"
  :cloudbees-api-key ~(.trim (slurp "/Users/sandoz/cloudbees/sandoz.apikey"))
  :cloudbees-api-secret ~(.trim (slurp "/Users/sandoz/cloudbees/sandoz.secret"))

  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.6.4"]]

  :dev-dependencies [[lein-ring "0.4.6"]
                     [lein-cloudbees "1.0.1"]]

  :ring {:handler mandel.core/app})

