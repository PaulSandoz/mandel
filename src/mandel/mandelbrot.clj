(ns mandel.mandelbrot
  (:import (java.awt.image BufferedImage WritableRaster)))

(defn- mandelbrot [^double cr ^double ci ^long limit]
  (loop [rate limit
         zr 0.0
         zi 0.0]
    (let [zr2 (* zr zr)
          zi2 (* zi zi)]
    (if (and (> rate 0) (< (+ zr2 zi2) 4.0))
      (recur (dec rate) (+ cr (- zr2 zi2)) (+ ci (* 2.0 (* zr zi))) )
      rate))))

; ([x y limit] [x y limit] ...)
(defn- mandelbrot-seq [x-range y-range [lr li] d limit]
  (for [y y-range :let [i (+ li (* y d))]
        x x-range :let [r (+ lr (* x d))]]
    [x y (mandelbrot r i limit)]))

;
;
(defn- delta [size [x1 y1] [x2 y2]]
  (max (/ (- x2 x1) size) (/ (- y2 y1) size)))

(defn- dimensions [size [x1 y1] [x2 y2]]
  (let [d (delta size [x1 y1] [x2 y2])]
    [(int (/ (- x2 x1) d)) (int (/ (- y2 y1) d))]))

(defn- normalize [[x1 y1] [x2 y2]]
  [[(min x1 x2) (min y1 y2)] [(max x1 x2) (max y1 y2)]])

(defn- ranges [l s]
  (partition 2 1 [l] (range 0 l s)))

; [w h
;  ([x y w h ([x y limit] [x y limit] ...)]
;   [x y w h ([x y limit] [x y limit] ...)] ...)]
(defn- mandelbrot-seqs [n size a b limit]
  (let [[lower upper] (normalize a b)
        d (delta size lower upper)
        [w h] (dimensions size lower upper)]
    [w h
     (for [[ly uy] (ranges h (int (/ h (min n h))))]
       [ 0 ly w (- uy ly) (mandelbrot-seq (range 0 w) (range ly uy) lower d limit) ]
       )]))

;
;
(defn text-mandel [size a b limit]
  (let [[w _ ms] (mandelbrot-seqs 1 size a b limit)
        [_ _ _ _ m] (first ms)
         s (map (fn [[_ _ i]]
                         (if (zero? i) " " (str (int (* 9 (/ i limit)))))) m)]
    (println
      (interpose \newline
        (map #(apply str %)
          (partition w s))))))

;
;
(def PERIOD (* 8.0 Math/PI))

(defn- ^double angle [^long rate ^long limit] (/ (* rate PERIOD) limit))

(defn- ^long sample [^double angle] (+ (* (Math/sin angle) 128) 128))

(def THIRD-PI (/ Math/PI 3.0))

(def TWO-THIRD-PI (* 2.0 (/ Math/PI 3.0)))

(defn- rgb [^double angle]
  (let [v (int-array 3)]
    (if (not (zero? angle))
      (do
        (aset v 0 (int (sample angle)))
        (aset v 1 (int (sample (+ angle THIRD-PI))))
        (aset v 2 (int (sample (+ angle TWO-THIRD-PI))))))
    v))

(defn- image-mandel-doseq
  ([[dx dy w h m] limit]
    (let [image (BufferedImage. w h BufferedImage/TYPE_INT_RGB)]
      (image-mandel-doseq dx dy m image limit)))
  ([dx dy m ^BufferedImage image limit]
    (let [raster (.getRaster image)]
      (doseq [[x y rate] m]
        (.setPixel raster (int (- x dx)) (int (- y dy)) (ints (rgb (angle rate limit)))))
      [dx dy image])))

(defn- set-rect [[x y ^BufferedImage image] ^WritableRaster raster]
  (.setRect raster x y (.getRaster image)))

(defn image-mandel
  ([size a b limit]
    (let [[_ _ ms] (mandelbrot-seqs 1 size a b limit)
          [_ _ image] (image-mandel-doseq (first ms) limit)]
        image))
  ([n size a b limit]
    (let [[w h ms] (mandelbrot-seqs n size a b limit)
          image (BufferedImage. w h BufferedImage/TYPE_INT_RGB)
          raster (.getRaster image)]
      (dorun
        (map #(set-rect % raster)
          (pmap #(image-mandel-doseq %1 limit) ms)))
      image)))

