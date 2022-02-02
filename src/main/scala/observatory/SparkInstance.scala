package observatory

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

import java.util.concurrent.ForkJoinPool
import scala.collection.parallel.ForkJoinTaskSupport

object SparkInstance {

  import org.apache.log4j.{Level, Logger}

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)


  val conf: SparkConf = new SparkConf().setMaster("local[4]").setAppName("Observatory")
  val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
  val sparkContext: SparkContext = spark.sparkContext

//  val parPool = new ForkJoinPool()
//  val parTaskSupport = new ForkJoinTaskSupport(parPool)
}