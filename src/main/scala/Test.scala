package com.test.spark
import java.util.Calendar
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.log4j.{Level,Logger}
import net.liftweb.json._

object KafkaWorker{
	def main(args: Array[String]){
		// 引数判定 Zookeeper URLとTopic nameが指定していない場合は強制終了
		if(args.length < 2){
			println("Please your zookeeper URL, Topic name")
			System.exit(1)
		}
		
		// 引数を各変数に格納
		val Array(zkQuorum, topics) = args

		// ログレベルの変更
		Logger.getRootLogger.setLevel(Level.WARN)

		//　Sparkの準備
		val sparkConf = new SparkConf().setAppName("KafkaWorker")
		// ５秒ごとに処理
		val ssc = new StreamingContext(sparkConf,Milliseconds(5000))

		// Kafkaからデータ入力するためのユーティリティを利用しDStreamを定義
		// ssc,<YourZookeeperUrl>,default,Map(<topic> -> 1)
		val kafkaStream = KafkaUtils.createStream(ssc,zkQuorum,"default",Map(topics -> 1))

		// ログを分解しメソッド，ホスト，ステータスごとにまとめる
		val ParseRDD = kafkaStream.flatMap{case(null,value) => {
			val features : scala.collection.mutable.ArrayBuffer[String] = new collection.mutable.ArrayBuffer[String]()
			features += parseMethod(value)
			features += parseHost(value)
			features += parseStatus(value)
			(features)
		}}

		// まとめたRDDをカウントする
		val kafkaRDD = ParseRDD.map((_, 1)).reduceByKey(_ + _).map{
				case(word,count) => (count, word)
			}.transform(_.sortByKey(false)).map {
				case(count,word) => (word, count)
			}
		
		// チェックポイントの設定
		ssc.checkpoint("/path/to/file/checkpoint")

		// 表示
		kafkaRDD.foreachRDD ( rdd => {
			println("\n##Start %s ###".format(Calendar.getInstance.getTime.toString))
			val path = rdd.collect()
			path.foreach{case (value,count) => println("%s \t %s".format(value,count))}
			println("##End %s ###".format(Calendar.getInstance.getTime.toString))
		})

		// 処理を実行
		ssc.start()

		// 処理が終了するのを待つ
		ssc.awaitTermination()
	}

	// parse定義用
	case class FluentEvent(domain: String, host: String, server: String, ident: String, user: String, method: String, path: String, protocol: String, status: String, size: String, referer: String, agent: String, response_time: String, cookie: String, set_cookie: String)
	
	// デフォルトのフォーマット
	implicit val formats = DefaultFormats

	// parse method
	def parseMethod(record: String) ={
		var method = parse(record).extract[FluentEvent].method
		(method)
	}

	// parse Host
	def parseHost(record: String) = {
		var host = parse(record).extract[FluentEvent].host
		(host)
	}

	// parse status
	def parseStatus(record: String) ={
		var status = parse(record).extract[FluentEvent].status
		(status)
	}
}
