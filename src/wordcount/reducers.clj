(ns wordcount.reducers)

; Copied from clojure.core/reducers (eugh!)
;;;;;;;;;;;;;; some fj stuff ;;;;;;;;;;

(defmacro ^:private compile-if
  "Evaluate `exp` and if it returns logical true and doesn't error, expand to
  `then`.  Else expand to `else`.

  (compile-if (Class/forName \"java.util.concurrent.ForkJoinTask\")
    (do-cool-stuff-with-fork-join)
    (fall-back-to-executor-services))"
  [exp then else]
  (if (try (eval exp)
           (catch Throwable _ false))
    `(do ~then)
    `(do ~else)))

(compile-if
 (Class/forName "java.util.concurrent.ForkJoinTask")
 ;; We're running a JDK 7+
 (do
   (def pool (delay (java.util.concurrent.ForkJoinPool.)))

   (defn fjtask [^Callable f]
     (java.util.concurrent.ForkJoinTask/adapt f))

   (defn- fjinvoke [f]
     (if (java.util.concurrent.ForkJoinTask/inForkJoinPool)
       (f)
       (.invoke ^java.util.concurrent.ForkJoinPool @pool ^java.util.concurrent.ForkJoinTask (fjtask f))))

   (defn- fjfork [task] (.fork ^java.util.concurrent.ForkJoinTask task))

   (defn- fjjoin [task] (.join ^java.util.concurrent.ForkJoinTask task)))
 ;; We're running a JDK <7
 (do
   (def pool (delay (jsr166y.ForkJoinPool.)))

   (defn fjtask [^Callable f]
     (jsr166y.ForkJoinTask/adapt f))

   (defn- fjinvoke [f]
     (if (jsr166y.ForkJoinTask/inForkJoinPool)
       (f)
       (.invoke ^jsr166y.ForkJoinPool @pool ^jsr166y.ForkJoinTask (fjtask f))))

   (defn- fjfork [task] (.fork ^jsr166y.ForkJoinTask task))

   (defn- fjjoin [task] (.join ^jsr166y.ForkJoinTask task))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- foldseq [num-tasks coll chunk-size combinef reducef]
  (fjinvoke (fn []
    (let [chunk->task (fn [chunk] (fjfork (fjtask #(reduce reducef (combinef) chunk))))]
      (reduce combinef 
        (map #(fjjoin (first (doall %)))
          (partition-all num-tasks 1 (map chunk->task (partition-all chunk-size coll)))))))))

(defn foldable-seq
  "Given a sequence, return a sequence that supports parallel fold.
  The sequence is consumed incrementally in chunks of size n
  (where n is the group size parameter passed to fold). No more than
  num-tasks (default 10) will be processed in parallel."
  ([coll] (foldable-seq 10 coll))
  ([num-tasks coll]
    (reify
      clojure.core.protocols/CollReduce
      (coll-reduce [_ f]
        (clojure.core.protocols/coll-reduce coll f (f)))
      (coll-reduce [_ f init]
        (clojure.core.protocols/coll-reduce coll f init))
      
      clojure.core.reducers/CollFold
      (coll-fold [_ n combinef reducef]
        (foldseq num-tasks coll n combinef reducef)))))
