package ru.beeline.dmp.datasource.noise.batch.text

import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{StreamingQueryException, Trigger}
import org.scalatest.{BeforeAndAfterAll, Ignore}
import org.scalatest.funsuite.AnyFunSuiteLike

import java.io.File
import scala.concurrent.TimeoutException
import scala.reflect.io.Directory

@Ignore
class NoiseBatchTest extends AnyFunSuiteLike with Logging with BeforeAndAfterAll {

  val checkPointDir = "/tmp/sparksftpreciever"

  override def beforeAll() {
    println("Before!") // start up your web server or whatever
    val directory = new Directory(new File(checkPointDir))
    directory.deleteRecursively()

  }

  test("Main batch datasource") {

    val spark = SparkSession
      .builder
      .appName("spark-datasource-batch-example")
      .master("local[2]")
      .getOrCreate

    val appId = spark.sparkContext.applicationId

    val filelistDF = spark.read
      .format("ru.beeline.dmp.datasource.noise.batch.text.DefaultSource")
      .option("key", java.util.UUID.randomUUID.toString)
      .option("partSize", "1000")
      .option("partNum", "5")
      .option("pattern", "7937{NUM(7)};{ENG(25,2)};{NUM(8)};{TS};{DT};1251{HEX(8,1)};{RUS(15,0)}")
      .load()

    println(filelistDF.count())
    filelistDF.show(5, 129, false)
    filelistDF.printSchema()
    assert(true, "Main batch datasource")
  }


  test("Main stream datasource") {

    val spark = SparkSession
      .builder
      .appName("spark-datasource-stream-example")
      .master("local[2]")
      .getOrCreate

    val appId = spark.sparkContext.applicationId

    val stream = spark.readStream
      .format("ru.beeline.dmp.datasource.noise.stream.text.DefaultSource")
      .option("key", java.util.UUID.randomUUID.toString)
      .option("partSize", "1000")
      .option("partNum", "5")
      .option("pattern", "7903{NUM(7)};{ENG(25,2)};{NUM(8)};{TS};{DT};1251{HEX(8,1)};{RUS(15,0)}")
      .load()

    try
      stream
//        .selectExpr("CAST( CONCAT('#',partition,'#',rownum, '#',timestamp) AS STRING) AS KEY", "CAST(value AS STRING) AS VALUE")
        .selectExpr("CAST(timestamp AS STRING) AS TIMESTAMP", "CAST(partition AS STRING) AS PARTITION", "CAST(rownum AS STRING) AS ROWNUM", "CAST(value AS STRING) AS VALUE")
        .writeStream
        .outputMode("append")
        .format("console")
        .option("truncate", "false")
        .option("numRows", 10)
        .trigger(Trigger.ProcessingTime("5 seconds"))
        .option("checkpointLocation", checkPointDir)
        .start
        .awaitTermination()

    catch {
      case e@(_: StreamingQueryException | _: TimeoutException) =>
        throw new RuntimeException(e)
    }


  }


}

