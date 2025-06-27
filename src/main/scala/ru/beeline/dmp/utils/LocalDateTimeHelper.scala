package ru.beeline.dmp.utils

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time._
import java.util.Date
import scala.util.Try

object LocalDateTimeHelper {

  def dateToLocalDateTime(dateToConvert: Date): java.time.LocalDateTime = {
    Instant.ofEpochMilli(dateToConvert.getTime())
      .atZone(ZoneId.systemDefault())
      .toLocalDateTime();
  }


  def LocalDateTimeToDate(dateToConvert: LocalDateTime): Date = {
    java.util.Date
      .from(dateToConvert.atZone(ZoneId.systemDefault())
        .toInstant());
  }


  def part2LocalDateTime(part: String, pattern: String = "yyyy-MM-dd--HH"): LocalDateTime = {
    LocalDateTime.parse(part, DateTimeFormatter.ofPattern(pattern))
  }

  def localDateTime2Part(event: LocalDateTime, pattern: String = "yyyy-MM-dd--HH"): String = {
    DateTimeFormatter.ofPattern(pattern).format(event)
  }

  def localDateTime2String(event: LocalDateTime, pattern: String = "yyyy-MM-dd'T'HH:mm:ss"): String = {
    DateTimeFormatter.ofPattern(pattern).format(event)
  }


  def part2LocalDate(part: String, pattern: String = "yyyy-MM-dd"): LocalDate = {
    LocalDate.parse(part, DateTimeFormatter.ofPattern(pattern))
  }

  def localDate2Part(event: LocalDate, pattern: String = "yyyy-MM-dd"): String = {
    DateTimeFormatter.ofPattern(pattern).format(event)
  }

  def string2LocalDateTime(part: String, pattern: String = "yyyy-MM-dd'T'HH:mm:ss"): LocalDateTime = {
    LocalDateTime.parse(part, DateTimeFormatter.ofPattern(pattern))
  }

  def localDateTime2Timestamp(event: LocalDateTime): Timestamp = {
    Timestamp.valueOf(event)
  }

  private def periodParse(feString: String) =
    if (Character.isUpperCase(feString.charAt(feString.length - 1))) Period.parse("P" + feString)
    else Duration.parse("PT" + feString)

  def localDateTimeOffset(event: LocalDateTime, offsetValue: String): LocalDateTime = {

    val amount = periodParse(offsetValue)
    Try({
      event.plus(amount)
    }).get

  }

  //     for (offset <- 0l to stepCount by (stepCount % stepCount.abs))


  def localDateTimesInWindow(event: LocalDateTime, window: String): List[LocalDateTime] = {
    val unit = window.replaceAll("""[0-9-+]+""", "")
    unit match {
      case "D" => {
        val stepCount = ChronoUnit.DAYS.between(event, localDateTimeOffset(event, window))
        if (stepCount >= 0) (for (offset <- 0l to stepCount) yield event.plusDays(offset.abs)).toList.distinct
        else (for (offset <- stepCount to 0l) yield event.minusDays(offset.abs)).toList.distinct
      }
      case "h" => {
        val stepCount = ChronoUnit.HOURS.between(localDateTimeOffset(event, window), event)
        if (stepCount >= 0) (for (offset <- 0l to stepCount) yield event.plusHours(offset.abs)).toList.distinct
        else (for (offset <- stepCount to 0l) yield event.minusHours(offset.abs)).toList.distinct
      }
      case "m" => {
        val stepCount = ChronoUnit.MINUTES.between(localDateTimeOffset(event, window), event)
        if (stepCount >= 0) (for (offset <- 0l to stepCount) yield event.plusMinutes(offset.abs)).toList.distinct
        else (for (offset <- stepCount to 0l) yield event.minusMinutes(offset.abs)).toList.distinct
      }
      case _ => List(event)
    }
  }

}


/**
 * Letter	Date or Time Component	Presentation	Examples
 * G	      Era designator	        Text	        AD
 * y	      Year	                  Year	        1996; 96
 * Y	      Week year	              Year	        2009; 09
 * M	      Month in year	          Month	        July; Jul; 07
 * w	      Week in year	          Number	      27
 * W	      Week in month	          Number	      2
 * D	      Day in year	            Number	      189
 * d	      Day in month	          Number	      10
 * F	      Day of week in month	  Number        2
 * E	      Day name in week	      Text	        Tuesday; Tue
 * u	      Day number of week       (1 = Monday, ..., 7 = Sunday)	Number	1
 * a	      Am/pm marker	          Text	         PM
 * H	      Hour in day (0-23)	    Number  	     0
 * k	      Hour in day (1-24)	    Number	      24
 * K	      Hour in am/pm (0-11)	  Number	      0
 * h	      Hour in am/pm (1-12)	  Number	      12
 * m	      Minute in hour	        Number	      30
 * s	      Second in minute	      Number	      55
 * S	      Millisecond	            Number	      978
 * z	      Time zone	General time zone	Pacific Standard Time; PST; GMT-08:00
 * Z	      Time zone	RFC 822 time zone	-0800
 * X	      Time zone	ISO 8601 time zone	-08; -0800; -08:00
 */

