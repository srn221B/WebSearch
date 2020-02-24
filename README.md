${SPARK_HOME}/bin/spark-submit --master yarn-client --class com.test.spark.KafkaWorker target/scala-2.10/WebSearch-assembly-0.1.jar <ZookeeperURL:PORT> <TopicName>
