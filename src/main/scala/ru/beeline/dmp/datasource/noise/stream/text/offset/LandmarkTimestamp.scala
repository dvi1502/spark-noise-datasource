package ru.beeline.dmp.datasource.noise.stream.text.offset

import java.time._

case class LandmarkTimestamp(
                              startOffset: Long,
                              endOffset: Long,
                              earliestOffset: Long,
                              latestOffset: Long,
                              currentTimestampProtected: Long,
                              systemTimestamp: Long = System.currentTimeMillis(),
                              batchSize: Int,
                              protectiveDelay: Long,
                              lag: Long,
                            ) {
  def pretty(): String =
    classOf[LandmarkTimestamp]
      .getDeclaredFields
      .map { f =>
        f.setAccessible(true)
        val res = (f.getName, f.get(this), f.getGenericType.getTypeName)
        f.setAccessible(false)
        res._3 match {
          case "long" if (res._2.toString.toLong < 1740000000000l) => {
            val duration = Duration.ofMillis(res._2.toString.toLong)
            val DD = duration.toDays
            val seconds = duration.getSeconds() - (DD * 3600 * 24)
            val HH = seconds / 3600
            val MM = (seconds % 3600) / 60
            val SS = seconds % 60
            val days = if (DD > 0) s"${DD} day" else ""
            f"${res._1.padTo(25, ' ')}\t:\t${days} ${HH}%02d:${MM}%02d:${SS}%02d"
          }
          case "long" => s"${res._1.padTo(25, ' ')}\t:\t${res._2}\t[ ${LocalDateTime.ofInstant(Instant.ofEpochMilli(res._2.toString.toLong), ZoneId.systemDefault())} ] "
          case "int" => s"${res._1.padTo(25, ' ')}\t:\t${res._2} "
          case _ => s"${res._1.padTo(25, ' ')}\t:\t${res._2} "
        }

      }.mkString("\n") + "\n---\n"
}

object LandmarkTimestamp {
  def apply(fileList: List[Long], startOffset: Long, batchSize: Int, protectiveDelay: Long): LandmarkTimestamp = {
    val currentTimestampProtected: Long = getLatestTimestamp(protectiveDelay)

    val earliestOffset: Long =
      fetchEarliestOffset(fileList, startOffset) match {
        case Some(value) if value < startOffset => startOffset
        case Some(value) => value
        case None => startOffset
      }

    val latestOffset: Long =
      fetchLatestOffset(fileList, currentTimestampProtected) match {
        case Some(value) if value > currentTimestampProtected => currentTimestampProtected
        case Some(value) => value
        case None => currentTimestampProtected
      }

    val endOffset: Long =
      fetchSpecificOffset(fileList, startOffset, batchSize, currentTimestampProtected) match {
        case Some(value) => value + 1l
        case None => latestOffset
      }

    LandmarkTimestamp(
      startOffset = startOffset,
      endOffset = endOffset,
      earliestOffset = earliestOffset,
      latestOffset = latestOffset,
      currentTimestampProtected = currentTimestampProtected,
      batchSize = batchSize,
      protectiveDelay = protectiveDelay,
      lag = (latestOffset - startOffset),
    )

  }

  def fetchLatestOffset(fileList: List[Long], currentTimestampProtected: Long): Option[Long] = {
    fileList
      .filter(_ < currentTimestampProtected)
      .reduceLeftOption(_ max _)
  }

  def fetchSpecificOffset(fileList: List[Long], startOffset: Long, batchSize: Int, currentTimestampProtected: Long): Option[Long] = {
    val l = fileList
      .filter(_ >= startOffset)
      .filter(_ < currentTimestampProtected)
      .sortBy(f => f)

    l.lift(Math.min(l.length, batchSize) - 1)
  }

  def fetchEarliestOffset(fileList: List[Long], startOffset: Long): Option[Long] = {
    fileList.filter(_ >= startOffset).reduceLeftOption(_ min _)
  }

  def getLatestTimestamp(protectiveDelay: Long): Long = {
    (ZonedDateTime.now().toInstant.toEpochMilli - protectiveDelay) / 1000L * 1000L
    //    LocalDateTime.now().atZone(TimeZone.getDefault.toZoneId).toInstant.toEpochMilli / 1000L * 1000L
  }

}