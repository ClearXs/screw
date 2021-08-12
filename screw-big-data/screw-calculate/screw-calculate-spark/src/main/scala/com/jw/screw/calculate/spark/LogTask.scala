package com.jw.screw.calculate.spark

import com.jw.screw.calculate.spark.model.SourceStatistics
import com.jw.screw.common.util.Collections
import com.jw.screw.logging.core.model.Message
import org.apache.spark.SparkContext

import java.util
import scala.collection.JavaConversions

class LogTask {

  /**
   * 统计source
   * @param logs 日志数据
   * @return 按次数统计来源
   */
  def statisticSource(message: java.util.List[Message]): java.util.List[SourceStatistics] = {
    if (Collections.isEmpty(message)) {
      return new util.ArrayList[SourceStatistics]()
    }
    val sc: SparkContext = SparkFactory.build
    try JavaConversions.seqAsJavaList(sc.makeRDD(JavaConversions.asScalaBuffer(message).toList, 3)
      .groupBy(_.getSource)
      .mapValues(_.toList.size)
      .map(tuple => {
        val statistics: SourceStatistics = new SourceStatistics()
        statistics.setCount(tuple._2)
        statistics.setSource(tuple._1)
        statistics
      })
      .collect()
      .seq)
    finally sc.stop()
  }

  /**
   * 统计某个事件范围内服务日志记录次数
   * @param statistics
   */
  def statisticsSameServiceTimeRangeCount(statistics: java.util.List[SourceStatistics]): Int = {
    if (Collections.isEmpty(statistics)) {
      return 0
    }
    val sc: SparkContext = SparkFactory.build
    try sc.makeRDD(JavaConversions.asScalaBuffer(statistics).toList, 3).map(_.getCount).reduce(_ + _)
    finally sc.stop()
  }
}
