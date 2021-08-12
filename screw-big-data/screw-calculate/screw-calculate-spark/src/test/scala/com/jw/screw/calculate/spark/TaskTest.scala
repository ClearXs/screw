package com.zzht.patrol.screw.calculate.spark

object TaskTest {
  //  def main(args: Array[String]): Unit = {
  //    val task = new SimpleTask
  //    val all = task.getTask
  //    // 日志内容中有content [DEBUG] END --> START
  //    // 去除这个记录并统计每个内容数据出现的次数
  //    val newRDD = all
  //      // 过滤[DEBUG] END --> START数据
  //      .filter(x => {
  //        x.getContent != null && !x.getContent.equals("[DEBUG] END --> START")
  //      })
  //      // 返回{id, content}数据
  //      .map(x => (x.getContent, x.getId))
  //      // value = 1
  //      .mapValues(v => 1)
  //      // 数据相加
  ////      .combineByKey(_, (x: Int, y: Int) => x + y, (x: Int, y: Int) => x + y)
  ////      .reduceByKey((x: Int, y: Int) => {
  ////          x + y
  ////      })
  //    println(newRDD.collect().toList)
  //  }
}
