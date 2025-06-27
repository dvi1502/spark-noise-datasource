package ru.beeline.dmp.utils

import com.typesafe.config.Config
import org.apache.spark.internal.Logging
import org.json4s.BuildInfo

import java.util

case class NoiseProperties(
                            key: String
                            , partSize: Int //= 1000,
                            , partNum:Int //= 5
                            , pattern: String //= "7937{NUM(7)};{ENG(25,2)};{NUM(8)};{TS};{DT};1251{HEX(8,1)};{RUS(15,0)}",
                          ) {

  def getTransactionId: Long = System.currentTimeMillis()

  def pretty(): String = {
    val sb = new StringBuffer()
    sb.append("\n >>> Lib Build info: " + BuildInfo.toString + "\n")
    sb.append(
      classOf[NoiseProperties]
        .getDeclaredFields
        .map { f =>
          f.setAccessible(true)
          val res = (f.getName, f.get(this))
          f.setAccessible(false)
          s"${res._1.padTo(25, ' ')}\t:\t${res._2}"
        }.mkString("\n") + "\n---\n"
    )
    sb.toString
  }

  private var hash = 0

  override def hashCode(): Int = {
    if (this.hash != 0) return this.hash
    val prime = 31
    var result = prime + this.key.hashCode
    result = prime * result + this.pattern.hashCode
    this.hash = result;
    result;
  }

  override def toString(): String = this.key

}


object NoiseProperties extends Logging {

  def apply(properties: util.Map[String, String]): NoiseProperties = {
    import scala.collection.JavaConverters._
    val parameters = properties.asScala.toMap
    NoiseProperties.atSpark(properties)
  }


  def toMap(config: Config): Map[String, String] = {
    import scala.collection.JavaConverters.asScalaSetConverter
    config.entrySet()
      .asScala
      .map(e => e.getKey.replace("\"", "") -> e.getValue.unwrapped().toString)
      .toMap
  }

  def atSpark(properties: util.Map[String, String]): NoiseProperties = {
    import scala.collection.JavaConverters._
    val parameters = properties.asScala.toMap

    val key: String = parameters.getOrElse("key", sys.error("'key' must be specified"))
    val partSize: Int = parameters.getOrElse("partSize", sys.error("'partSize' must be specified")).toInt
    val partNum: Int = parameters.getOrElse("partNum", sys.error("'partNum' must be specified")).toInt
    val pattern: String = parameters.getOrElse("pattern", sys.error("'pattern' must be specified"))

    val z = NoiseProperties(
      key = key,
      partSize = partSize,
      partNum = partNum,
      pattern = pattern
    )

    //    logInfo(s"""${z.pretty()}""")

    z

  }

}


