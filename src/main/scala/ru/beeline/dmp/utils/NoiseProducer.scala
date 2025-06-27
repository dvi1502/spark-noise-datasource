package ru.beeline.dmp.utils


import ru.beeline.dmp.utils.StringWrapper.CamelCaseWrapper
import java.time.{LocalDate, LocalDateTime}
import scala.util.Random

sealed trait AnyFieldProducer {
  protected def getval: Any
}


object DateTimeProducer extends AnyFieldProducer {
  override def getval: LocalDateTime = {
    LocalDateTime.of(
      2021 + Random.nextInt(2025 - 2021 + 1),
      Random.nextInt(12),
      Random.nextInt(28),
      Random.nextInt(23),
      0,
      0
    )
  }
}


object AnyFieldProducer {


  lazy val chrEng = ('a' to 'z').toArray[Char] ++ Array[Char](' ') //ENGxx
  lazy val chrRus = ('а' to 'я').toArray[Char] ++ Array[Char](' ') //RUSxx
  lazy val hex = ('0' to '9').toArray[Char] ++ ('a' to 'f').toArray[Char] //HEXxx
  lazy val digits = ('0' to '9').toArray[Char] //NUMxx
  lazy val chrSpec = Array[Char](' ', ',', '.', '!', '@', '-', '_') //Sxx


  private def getArgs(str: String) = {
    str
      .replace("(", "")
      .replace(")", "")
      .split(",")
      .map(s => s.toInt)
  }

  private def getRandomNumberUsingNextInt(min: Int, max: Int) = {
    //      ((Math.random * (max - min)) + min).asInstanceOf[Int]
    val random = new scala.util.Random();
    random.nextInt(max - min) + min
  }


  private def setRegister(v: String, register: Int = 0) = {
    register match {
      case 0 => v.toLowerCase
      case 1 => v.toUpperCase
      case 2 => v.toCamelCase
    }
  }

  def getHEX(len: Int, register: Int = 0) = {
    setRegister(
      (0 until len).map(f => hex(Random.nextInt(hex.size)).toString).mkString,
      register
    )
  }

  def getSentence(len: Int, language: Int = 0, register: Int = 0) = {

    setRegister((
      language match {
        case 0 => (0 until len).map(f => chrEng(Random.nextInt(chrEng.size)).toString).mkString
        case 1 => (0 until len).map(f => chrRus(Random.nextInt(chrRus.size)).toString).mkString
        case 2 => (0 until len).map(f => {
          val mixed = chrRus ++ chrEng ++ chrSpec
          mixed(Random.nextInt(mixed.size)).toString
        }).mkString
      }),
      register
    )

  }


  /**
   * @param pattern
   * передется шаблон нужной строки с мнемониками
   * {NUM(7)} {ENG(17)} {HEX17}
   * например
   * IP = "19{NUM(1)}.1{NUM(2)}.2{NUM(2)}.2{NUM(2)}"
   * MSISDN = +7919{NUM07}
   * ФИО А{CHR07} И{CHR04} И{CHR04}вич
   * @return
   */
  def genvalue(actualString: String = "7937;{ENG(17)};{NUM(8)};{TS}"): String = {

    val pattern = """\{([A-Z0-9]+)([\(,0-9\)]*)\}""".r

    pattern
      .replaceAllIn(
        actualString, s => {
          //          println(s.group(1))
          //          println(s.group(2))
          s.group(1).toString match {
            case "TS" => LocalDateTimeHelper.localDateTime2String(LocalDateTime.now())

            case "DT" => LocalDateTimeHelper.localDate2Part(LocalDate.now())

            case "NUM" => {
              val args = getArgs(s.group(2))
              if (args.length == 1)
                (0 until args(0)).map(f => digits(Random.nextInt(digits.size)).toString).mkString
              else
                (0 until args(0)).map(f => digits(Random.nextInt(digits.size)).toString).mkString + "." +
                  (0 until args(1)).map(f => digits(Random.nextInt(digits.size)).toString).mkString
            }

            case "HEX" => {
              val args = getArgs(s.group(2))
              args.length match {
                case 1 => getHEX(args(0))
                case 2 => getHEX(args(0), args(1))
              }
            }

            case "ENG" => {
              val args = getArgs(s.group(2))
              args.length match {
                case 1 => getSentence(args(0), language = 0, register = 0)
                case 2 => getSentence(args(0), language = 0, register = args(1))
              }
            }

            case "RUS" => {
              val args = getArgs(s.group(2))
              args.length match {
                case 1 => getSentence(args(0), language = 1, register = 0)
                case 2 => getSentence(args(0), language = 1, register = args(1))
              }
            }

            case "RND" => {
              val args = getArgs(s.group(2))
              getRandomNumberUsingNextInt(args(0), args(1)).toString
            }

          }
        })

  }
}

object StringWrapper {

  val useMapReduce: String => String = { spacedString =>
    val first :: rest = spacedString.split(Array(' ', '_')).toList.map(_.toLowerCase)
    val changedRest = rest.map(w => w.take(1).toUpperCase + w.drop(1))
    val reunited = first :: changedRest
    reunited.mkString
  }

  implicit class CamelCaseWrapper(val spacedString: String) extends AnyVal {

    def toCamelCase: String = useMapReduce(spacedString)
  }

}