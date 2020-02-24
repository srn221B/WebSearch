## WebSerch
Collect Web logs in real time.  

## Description
based on the book "[Apache Spark入門 動かして学ぶ最新並列分散処理フレームワーク]<https://www.shoeisha.co.jp/book/detail/9784798142661>".  
Analyze logs on Apache httpd Server using SparkStreaming.  

## Usage
OS : CentOS7  
Apache httpd : 2.4.6  
Fluentd td-agent : 0.12.40  
Kafka : 2.11  
Zookeeper : 3.4  
Spark : 1.5.0  

* Edit log settings on WebServer  
/etc/httpd/conf/httpd.conf  Added to log_config_module  
```
LogFormat "domain:%V\thost:%h\tserver:%A\tident:%l\tuser:%u\ttime:%{%d/%b/%Y:%H:%M:%S %z}t\tmethod:%m\tpath:%U%q\tprotocol:%H\tstatus:%>s\tsize:%b\treferer:%{Referer}i\tagent:%{User-Agent}i\tresponse_time:%D\tcookie:%{cookie}i\tset_cookie:%{Set-Cookie}o" apache_ltsv
CustomLog "logs/access_log_ltsv" apache_ltsv
```  

* Set Permissions  
access_log_ltsv is created under /var/log/httpd and logs are written.Set permissions to read with Fluentd.  
`
cd /var/log/httpd
sudo chmod o+rx access_log_ltsv
`  

* Setting fluentd  
/etc/td-agent/td-agent.conf
```
// ログの収集方法を設定
<source>
  @type tail
  # 入力ファイル名
  path /var/log/httpd/access_log_ltsv
  #メッセージ形式
  format ltsv
  # 日付時間形式
  time_key time
  time_format %d/%b/%Y:%H:%M:%S %z
  # 読み込み位置保存ファイル先
  pos_file /var/log/td-agent/access2.pos
  # イベントタグ
  tag apache2.access
</source>
// FluentdからKafkaに流されるようにする 
<match apache2.access>
  # Kafka出力
  @type kafka
  # IPアドレス:ポート番号
  brokers localhost:9092
  zookeeper localhost:2181
  # topic名
  default_topic accesslog-topic
</match>
```  

* Setting Kafka  
Zookeeper activation  
```
$ sudo bin/zkServer.sh start
```  
Kafka activation  
```
// トピックを作成
$ sudo bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic accesslog-topic
// トピックが作成されていることを確認
$ sudo bin/kafka-topics.sh --list --zookeeper localhost:2181
// Kafka起動
$ sudo bin/kafka-server-start.sh config/server.properties
```  

* Run  
```
${SPARK_HOME}/bin/spark-submit --master yarn-client --class com.test.spark.KafkaWorker target/scala-2.10/WebSearch-assembly-0.1.jar IPアドレス:2181 accesslog-topic

・・・
##End Mon Jul 29 05:40:45 JST 2019 ###

##Start Mon Jul 29 05:40:50 JST 2019 ###
192.168.10.110 	 2
POST 	 1
404 	 1
200 	 1
GET 	 1
##End Mon Jul 29 05:40:50 JST 2019 ###
・・・
```  
## Author
shimoyama
