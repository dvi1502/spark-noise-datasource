package ru.beeline.dmp.datasource.noise.stream.text.offset

import org.apache.spark.sql.connector.read.streaming.Offset

import scala.util.control.NonFatal


class NoiseSourceOffsetPilot(_offsets: scala.collection.mutable.Map[String, Long]) extends Offset {

  def setOffset(key: String, value: Long): Unit = {
    this._offsets += (key -> value)
  }

  def removeOffset(key: String): Unit = {
    this._offsets -= key
  }

  def getOffset(key: String): Option[Long] = {
    this._offsets.get(key)
  }

  def existsOffset(key: String): Boolean = {
    this._offsets.get(key) match {
      case Some(value) => true
      case _ => false
    }
  }

  def get: Map[String, Long] = {
    this._offsets.toMap
  }

  def set(_values: Map[String, Long]): Unit = {
    _values.foreach {
      case (k, v) => this.setOffset(k, v)
    }
  }

  override def toString: String = {
    import java.time.{Instant, LocalDateTime, ZoneId}
    this._offsets
      .map(x => s"${x._1}; ${x._2}; ${LocalDateTime.ofInstant(Instant.ofEpochMilli(x._2), ZoneId.systemDefault())}")
      .toList.mkString("\n")
  }

  def toJson: String = {
    import org.json4s.NoTypeHints
    import org.json4s.jackson.Serialization
    implicit val formats = Serialization.formats(NoTypeHints)
    Serialization.write(this._offsets.toMap)
  }

  override def json(): String = this.toJson

}

object NoiseSourceOffsetPilot {

  def create(): NoiseSourceOffsetPilot = {
    val _offsets = scala.collection.mutable.Map[String, Long]()
    new NoiseSourceOffsetPilot(_offsets)
  }

  def create(_values: Seq[(String, Long)]): NoiseSourceOffsetPilot = {
    new NoiseSourceOffsetPilot(collection.mutable.Map(_values: _*))
  }

  def create(_json: String): NoiseSourceOffsetPilot = {
    import org.json4s.NoTypeHints
    import org.json4s.jackson.Serialization
    implicit val formats = Serialization.formats(NoTypeHints)

    val _offsets = scala.collection.mutable.Map[String, Long]()

    try {
      val off = Serialization.read[Map[String, Long]](_json)
      off.foreach(x => _offsets += x)
      new NoiseSourceOffsetPilot(_offsets)
    } catch {
      case NonFatal(ex) =>
        throw new IllegalArgumentException(
          s"""${ex.getMessage}: Expected e.g. {"serverA/upload/10":23},{"serverA/upload/11":-1},{""serverB"/upload/13":-2}, got '${_json}'"""
        )
    }

  }

}
