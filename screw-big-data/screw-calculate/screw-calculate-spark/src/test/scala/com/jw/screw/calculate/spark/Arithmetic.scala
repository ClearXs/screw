package com.jw.screw.calculate.spark

import com.jw.screw.common.util.FileUtils
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{Partition, SparkConf, SparkContext}

class Arithmetic {

  private def getSparkContext: SparkContext = {
    val conf = new SparkConf()
    conf.setMaster("local")
    conf.setAppName("test")
    new SparkContext(conf)
  }

  // 创建算子

  def makeRDD(): Unit = {
    val sc = getSparkContext
    // ---- makeRDD算子，创建RDD
    // seq：集合数据集
    // numSlices：分区数，如果不指定默认使用spark配置中 spark.default.parallelism的数据
    val rdd = sc.makeRDD(1 to 6, 3)
    // collect
    val collect = rdd.collect
    // print 1 2 3 4 5 6
    collect.foreach((i: Int) => {
      println(i)
    })
    // partitions
    val partitions = rdd.partitions
    partitions.foreach((p: Partition) => {
      println(p)
    })
    // 生成2个分区，其中分区1存储：1 to 6存储在host1，host2上，分区2存储：7 to 10存储在host3上
    val data = Seq((1 to 6, Seq("host1", "host2")), (7 to 10, Seq("host3")))
    // 重写构建RDD
    val newRdd = sc.makeRDD(data)
    val newCollect = newRdd.collect
    newCollect.foreach((f: Range.Inclusive) => {
      // print Range(1, 2, 3, 4, 5, 6) Range(7, 8, 9, 10)
      println(f)
    })
    // print List(host1, host2)
    println(newRdd.preferredLocations(partitions(0)))
    // print List(host3)
    println(newRdd.preferredLocations(partitions(1)))
  }

  def parallelize(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(1 to 6, 3)
    val collect = rdd.collect
    for (i: Int <- collect) {
      println(i)
    }

    val partitions = rdd.partitions
    for (partition <- partitions) {
      println(partition)
    }
  }

  def textFile(): Unit = {
    val sc = getSparkContext
    val path = FileUtils.getClassPath("classpath:log4j.properties")
    val rdd = sc.textFile(path)
    println(rdd.count())
    // 显示第一行内容
    println(rdd.first())
  }

  // =========== value型变换算子 ===========

  /**
   * map对RDD每个元素进行作用，返回新的RDD
   */
  def map(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List(1, 2, 3, 4), 2)
    // 作用于当前每个RDD的元素，并返回新的RDD
    val collect = rdd.map(i => {
      i + 1
    }).collect()
    for (elem <- collect) {
      println(elem)
    }
  }

  /**
   * 重新分区，生成新的RDD
   */
  def coalesce(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List(1, 2, 3, 4), 4)
    // print 4
    println(rdd.partitions.length)
    // 重新分区
    val newRdd = rdd.coalesce(2, shuffle = false)
    // print 2
    println(newRdd.partitions.length)
  }

  /**
   * 元素去重，返回新的RDD
   */
  def distinct(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List(1, 1, 2, 3, 4), 4)
    val newRdd = rdd.distinct(2).collect()
    println(newRdd.toList)
  }

  def filter(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List(2, 2, 2, 3), 4)
    // print 3
    // 输出每个分区的数据
    println(rdd.glom().collect().toList)
    val newRdd = rdd.filter(_ != 2)
    println(newRdd.collect().toList)
  }

  def flatMap(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List(1, 2, 3))
    // 返回多个序列，多个序列形成新的RDD
    // 而map返回的使一个
    val newRdd = rdd.flatMap(x => 0 to x)
    println(newRdd.collect().toList)
  }

  def sample(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List(1, 2, 3, 4, 5))
    // 随机取样
    val newRdd = rdd.sample(false, 2)
    println(newRdd.collect().toList)
  }

  def sortBy(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List(2, 5, 3, 1), 1)
    val newRdd = rdd.sortBy(x => x)
    println(newRdd.collect().toList)
  }

  // =========== key/value型变换算子 ===========

  /**
   * 两个RDD元素进行笛卡尔积，返回结果元素的RDD
   */
  def cartesian(): Unit = {
    val sc = getSparkContext
    val rdd1 = sc.parallelize(List("a", "b", "c", "d"), 2)
    println(rdd1.collect().toList)
    val rdd2 = sc.parallelize(List(1, 2, 3), 2)
    println(rdd2.collect().toList)
    val newRdd = rdd1.cartesian(rdd2)
    println(newRdd.collect().toList)
  }

  /**
   * 两个RDD的元素进行交集，返回交集元素的RDD
   */
  def intersection(): Unit = {
    val sc = getSparkContext
    val rdd1 = sc.parallelize(List(1, 2, 3), 2)
    val rdd2 = sc.parallelize(List(2, 3), 2)
    val newRDD = rdd1.intersection(rdd2)
    // print 2, 3
    println(newRDD.collect().toList)
  }

  /**
   * 两个RDD的元素进行差集，返回差集元素的RDD
   */
  def subtract(): Unit = {
    val sc = getSparkContext
    val rdd1 = sc.parallelize(List(1, 2, 3), 2)
    val rdd2 = sc.parallelize(List(2, 3), 2)
    val newRDD = rdd1.subtract(rdd2)
    // print 1
    println(newRDD.collect().toList)
  }

  /**
   * 两个RDD元素进行并集，返回并集元素的RDD
   */
  def union(): Unit = {
    val sc = getSparkContext
    val rdd1 = sc.parallelize(List(1, 2, 3), 2)
    val rdd2 = sc.parallelize(List(2, 3, 4), 2)
    val newRDD = rdd1.union(rdd2)
    println(newRDD.collect().toList)
  }

  /**
   * 联结
   */
  def zip(): Unit = {
    val sc = getSparkContext
    val rdd1 = sc.parallelize(0 to 4, 2)
    val rdd2 = sc.parallelize(5 to 9, 2)
    // 两个rdd元素长度需是一致的
    val newRDD = rdd1.zip(rdd2)
    // print List((0,5), (1,6), (2,7), (3,8), (4,9))
    println(newRDD.collect().toList)
  }

  def pairKeyValueByMap(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "balance", "coalesce"))
    val newRDD = rdd.map(x => (x(0), x))
    // print List((a,apple), (b,balance), (c,coalesce))
    println(newRDD.collect().toList)
  }

  def pairKeyValueByKeyBy(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "balance", "coalesce"))
    val newRDD = rdd.keyBy(_.length)
    // print List((5,apple), (7,balance), (8,coalesce))
    println(newRDD.collect().toList)
  }

  /**
   * 合并单个key/value元素
   */
  def combineByKey(): Unit = {
    val sc = getSparkContext
    // ("n", "apple") 称为二元组，所以集合存放的元素是二元组
    val rdd = sc.parallelize(List(("n", "apple"), ("b", "balance"), ("n", "coalesce")))
    // createCombiner: 依次处理每个<Key, Value>对， 如果Key是第一次出现，则触发createCombiner函数，将<Key, Value>转换由函数指定类型C List(_)：_ 接收参数，它接收的是二元组的第二个参数，所以最后存储的的数据是apple、balance。coalesce不会进行存储，因为已经触发过了
    // mergeValue： 如果对于<Key, Value>的Key不是第一次出现，则触发mergeValue，将createCombiner的结果类型C作为参数进行传递 y :: x 把元素y添加到集合x中，只能存储coalesce -> list("apple")中
    // mergeCombiners： 把所有分散的Key汇聚在一起与前面计算的结果C汇聚到一起进行计算  x ::: y 连接x y两个集合
    // 假设createCombiner计算得{apple} C1 mergeValue计算得{coalesce} C2，所以mergeCombiners计算得{apple, coalesce}，知道C1、C2都是得key是n，所以结果为{n, {apple, coalesce}}
    val newRDD = rdd.combineByKey(List(_), (x: List[String], y: String) => y :: x, (x: List[String], y: List[String]) => x ::: y)
    // print List((b,List(balance)), (n,List(coalesce, apple)))
    // 结果来看，以b、n开头的数据进行聚合
    println(newRDD.collect().toList)
  }

  def flatMapValues(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "balance")).keyBy(_.length)
    // {(5, "apple"), (7, ""balance)}
    // List((5,_), (5,a), (5,p), (5,p), (5,l), (5,e), (7,_), (7,b), (7,a), (7,l), (7,a), (7,n), (7,c), (7,e))
    val newRDD = rdd.flatMapValues(x => "_" + x)
    println(newRDD.collect().toList)
  }

  def groupByKey(): Unit = {
    val sc = getSparkContext
    // ("n", "apple") 称为二元组，所以集合存放的元素是二元组
    val rdd = sc.parallelize(List(("n", "apple"), ("b", "balance"), ("n", "coalesce")))
    // List((b,CompactBuffer((b,balance))), (n,CompactBuffer((n,apple), (n,coalesce))))
    val newRDD = rdd.groupBy(_._1)
    println(newRDD.collect().toList)
  }

  def keys(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "balance")).keyBy(_.length)
    val newRDD = rdd.keys
    // List(5, 7)
    println(newRDD.collect().toList)
  }

  def mapValues(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "balance")).keyBy(_.length)
    val newRDD = rdd.mapValues(x => x + " " + x(0).toUpper)
    // print List((5,apple A), (7,balance B))
    println(newRDD.collect().toList)
  }

  def reduceByKey(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "balance", "berry")).keyBy(_.length)
    val newRDD = rdd.reduceByKey(_ + " " + _)
    println(newRDD.collect().toList)
  }

  def sortByKey(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "balance", "pig")).keyBy(_.length)
    val newRDD = rdd.sortBy(_._1)
    // print List((3,pig), (5,apple), (7,balance))
    println(newRDD.collect().toList)
  }

  def values(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "balance", "pig")).keyBy(_.length)
    val newRDD = rdd.values
    // print List(apple, balance, pig)
    println(newRDD.collect().toList)
  }

  // =========== 多个key/value变换 ===========
  def cogroup(): Unit = {
    val sc = getSparkContext
    val rdd1 = sc.parallelize(List("apple", "balance", "pig")).keyBy(_.length)
    val rdd2 = sc.parallelize(List("big")).keyBy(_.length)
    val newRDD = rdd1.cogroup(rdd2)
    // print List((7,(CompactBuffer(balance),CompactBuffer())), (3,(CompactBuffer(pig),CompactBuffer(big))), (5,(CompactBuffer(apple),CompactBuffer())))
    println(newRDD.collect().toList)
  }

  def join(): Unit = {
    val sc = getSparkContext
    val rdd1 = sc.parallelize(List("apple", "balance", "pig")).keyBy(_.length)
    val rdd2 = sc.parallelize(List("big")).keyBy(_.length)
    val newRDD = rdd1.join(rdd2)
    // print List((3,(pig,big)))
    println(newRDD.collect().toList)
  }

  // =========== 行动算子 ===========
  def reduce(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(0 to 9, 5)
    // 计算0 to 9所有元素合
    // f -> T, T 第一个T原所有元素和，第二个T当前循环的元素
    val newRDD = rdd.reduce(_ + _)
    // print 45
    println(newRDD)
  }

  def aggregate(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(0 to 9, 3)
    // 显示每个分区的数据
    println(rdd.glom().collect().toList)
    // 计算每个分区的数据总和，在求出分区和最大的分区数
    // print 35
    println(rdd.aggregate(0)(_ + _, Math.max))
  }

  def collect(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(0 to 9, 3)
    // print List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    println(rdd.collect().toList)
  }

  def collectAsMap(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "big", "coalesce")).keyBy(_.length)
    // print List((8,coalesce), (5,apple), (3,big))
    println(rdd.collectAsMap().toList)
  }

  def count(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(0 to 9, 3)
    // print 10
    println(rdd.count())
  }

  def countByKey(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "big", "coalesce")).keyBy(_.length)
    // print 3
    println(rdd.countByKey())
  }

  def countByValue(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "big", "coalesce", "big")).keyBy(_.length)
    // print Map((8,coalesce) -> 1, (3,big) -> 2, (5,apple) -> 1)
    println(rdd.countByValue())
  }

  def first(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "big", "coalesce", "big"))
    // print apple
    println(rdd.first())
  }

  def glom(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "big", "coalesce", "big"), 2)
    println(rdd.glom().collect().toList)
  }

  def fold(): Unit = {
    val sc = getSparkContext
    val rdd = sc.parallelize(List("apple", "big", "coalesce", "big"), 2)
    // 不同分区与同分区之间数据合并的初始值为|
    // print |.|.apple.big.|.coalesce.big
    // |.在分区内还是分区外都会带入
    println(rdd.fold("|")(_ + "." + _))
  }

  def lookup(): Unit = {
    val sc = getSparkContext
    // key-value
    val rdd = sc.parallelize(List("apple", "big", "coalesce", "big"), 2).keyBy(_(0))
    // WrappedArray(apple)
    println(rdd.lookup('a'))
  }

  def cache(): Unit = {
    val sc = getSparkContext
    // key-value
    val rdd = sc.parallelize(List("apple", "big", "coalesce", "big"), 2).keyBy(_(0))
    rdd.persist(StorageLevel.MEMORY_ONLY_SER_2)
  }
}

object Arithmetic {

  def main(args: Array[String]): Unit = {
    val arithmetic = new Arithmetic
//    arithmetic.makeRDD()
//    arithmetic.parallelize()
//    arithmetic.textFile()
//    arithmetic.map()
//    arithmetic.coalesce()
//    arithmetic.distinct()
//    arithmetic.filter()
//    arithmetic.flatMap()
//    arithmetic.sample()
//    arithmetic.sortBy()
//    arithmetic.cartesian()
//    arithmetic.intersection()
//    arithmetic.subtract()
//    arithmetic.union()
//    arithmetic.zip()
//    arithmetic.pairKeyValueByMap()
//    arithmetic.pairKeyValueByKeyBy()
//    arithmetic.combineByKey()
//    arithmetic.flatMapValues()
//    arithmetic.groupByKey()
//    arithmetic.keys()
//    arithmetic.mapValues()
    arithmetic.reduceByKey()
//    arithmetic.sortByKey()
//    arithmetic.values()
//    arithmetic.cogroup()
//    arithmetic.join()
//    arithmetic.reduce()
//    arithmetic.aggregate()
//    arithmetic.collect()
//    arithmetic.collectAsMap()
//    arithmetic.count()
//    arithmetic.countByKey()
//    arithmetic.countByValue()
//    arithmetic.first()
//    arithmetic.glom()
//    arithmetic.fold()
//    arithmetic.lookup()
  }
}
