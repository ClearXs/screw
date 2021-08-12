package com.zzht.patrol.screw.calculate.spark

import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("name").setMaster("local")
    val sc = new SparkContext(conf)
    val rdd = sc.textFile("D:\\word.txt", 3)
    rdd
      .flatMap(line => line.split(" "))
      .map(word => (word, 1))
      .reduceByKey((a, b) => a + b)
      .saveAsTextFile("D:\\")
  }
}
