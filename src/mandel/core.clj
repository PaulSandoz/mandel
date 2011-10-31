(ns mandel.core
  (:use compojure.core)
  (:use [compojure.response :only [Renderable render]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [ring.util.response]
            [mandel.mandelbrot :as mandelbrot])
  (:import (java.io ByteArrayOutputStream ByteArrayInputStream)
           (java.awt.image BufferedImage)
           (javax.imageio ImageIO)))

(defn render-image [image]
  (let [out (ByteArrayOutputStream.)]
    (do (ImageIO/write image "png" out) (ByteArrayInputStream. (.toByteArray out)))))

; ":use" needed  for compojure.response
(extend-protocol Renderable
  BufferedImage
  (render [image _]
    (-> (ring.util.response/response (render-image image))
        (ring.util.response/content-type "image/png"))))

(defroutes main-routes
  (GET "/" []
      (mandelbrot/image-mandel 4 1024 [-2 -1] [0.5 1] 256))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app (handler/site main-routes))


