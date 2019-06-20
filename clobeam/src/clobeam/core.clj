(ns clobeam.core
  (:require
   [datasplash.core :as ds])
  (:import
   (org.apache.beam.sdk Pipeline)
   (org.apache.beam.sdk.io TextIO)
   (org.apache.beam.sdk.io.kafka LongStringKafkaIO CustomFunction_)
   (org.apache.beam.sdk.options PipelineOptionsFactory)
   (org.apache.beam.sdk.transforms DoFn$ProcessContext MapElements  Count Values ParDo)
   (org.apache.beam.sdk.values KV)
   (org.apache.kafka.common.serialization LongDeserializer)
   (org.apache.kafka.common.serialization StringDeserializer)))

(def tokenizer-pattern "[^\\p{L}]+")

(defn tokenizer-fn
  "Returns a function that corresponds to a Clojure filter operation inside a ParDo"
  []
  (fn [^DoFn$ProcessContext c]
    (let [words (-> (.element c)
                    (.split tokenizer-pattern))]
      (doseq [word words]
        (when-not (.isEmpty word)
          (.output c word))))))

(defn run-pipeline []
  (let [options (PipelineOptionsFactory/create)
        p (Pipeline/create options)
        kafka-io-transforms (-> (LongStringKafkaIO/read)
                                (.withBootstrapServers "localhost:9092")
                                (.withTopic "words")
                                (.withKeyDeserializer LongDeserializer)
                                (.withValueDeserializer StringDeserializer)
                                (.updateConsumerProperties {"auto.offset.reset" "earliest"})
                                (.withMaxNumRecords 5)
                                (.withoutMetadata))]
    (-> p
        (.apply kafka-io-transforms)
        (.apply (Values/create))
        (.apply "ExtractWords" (ParDo/of (ds/dofn (tokenizer-fn))))
        (.apply (Count/perElement))
        (.apply "FormatResults" (MapElements/via
                                 (CustomFunction_.
                                  '(fn [^KV input]
                                     (str (.getKey input) ":" (.getValue input))))))
        (.apply (-> (TextIO/write)
                    (.to "wordcounts"))))
    (-> (.run p)
          (.waitUntilFinish))))
