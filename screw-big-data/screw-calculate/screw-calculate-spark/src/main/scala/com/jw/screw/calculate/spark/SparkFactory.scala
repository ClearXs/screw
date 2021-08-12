package com.jw.screw.calculate.spark

import org.apache.spark.{SparkConf, SparkContext}

class SparkFactory {

  private var sc: SparkContext = null

  private def getSparkContext(appName: String = "app", master: String = "local"): SparkContext = {
    if (sc == null) {
      val conf = new SparkConf()
      conf.setAppName(appName)
      conf.setMaster(master)
      sc = new SparkContext(conf)
    }
    sc
  }
}

object SparkFactory {
  private val factory = new SparkFactory

  def build: SparkContext = {
    factory.getSparkContext()
  }
}
