(ns clobeam.core
  (:import
   ;  (com.google.common.collect ImmutableMap)
   (org.apache.beam.sdk Pipeline)
   (org.apache.beam.sdk.io TextIO)
   (org.apache.beam.sdk.io.kafka LongStringKafkaIO)
   (org.apache.beam.sdk.options PipelineOptions)
   (org.apache.beam.sdk.options PipelineOptionsFactory)
   (org.apache.beam.sdk.transforms DoFn$ProcessContext MapElements SimpleFunction Count Values ParDo)
   (org.apache.beam.sdk.values KV)
   (org.apache.kafka.common.serialization LongDeserializer)
   (org.apache.kafka.common.serialization StringDeserializer)))

(def tokenizer-pattern "[^\\p{L}]+")

(defn run-pipeline []
  (let [options (PipelineOptionsFactory/create)
        p (Pipeline/create options)
        kafka-io-transforms (-> (LongStringKafkaIO/read)    ; RUNTIME ERROR No matching method apply found taking 1 args for class org.apache.beam.sdk.io.kafka.AutoValue_KafkaIO_Read
                                (.withBootstrapServers "kafka:29092")
                                (.withTopic "words")
                                (.withKeyDeserializer LongDeserializer)
                                (.withValueDeserializer StringDeserializer)
                                (.updateConsumerProperties {"auto.offset.reset" "earliest"})
                                (.withMaxNumRecords 5)
                                .withoutMetadata)           ; RUNTIME ERROR No matching method apply found taking 1 args for class org.apache.beam.sdk.io.kafka.KafkaIO$TypedWithoutMetadata
        ]
    (-> p
        (.apply kafka-io-transforms)
        (.apply (Values/create))
        (.apply "ExtractWords" (ParDo/of (fn [^DoFn$ProcessContext c]
                                           (let [words (-> (.element c)
                                                           (.split tokenizer-pattern))]
                                             (doseq [word words]
                                               (when-not (.isEmpty word)
                                                 (.output c word)))))))
        #_(.apply (Count/perElement))
        #_(.apply "FormatResults" (fn [^KV input]
                                    (str (.getKey input) ":" (.getValue input))))
        #_(.apply (-> (TextIO/write)
                      (.to "wordcounts"))))

    #_(-> (.run p)
          (.waitUntilFinish))))
