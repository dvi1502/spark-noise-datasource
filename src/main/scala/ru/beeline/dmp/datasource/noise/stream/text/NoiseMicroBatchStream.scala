package ru.beeline.dmp.datasource.noise.stream.text

import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.streaming.{MicroBatchStream, Offset, ReadLimit, SupportsAdmissionControl}
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.util.LongAccumulator
import ru.beeline.dmp.datasource.noise.stream.text.offset.{LandmarkTimestamp, NoiseSourceInitialOffsetWriter, NoiseSourceOffsetPilot}
import ru.beeline.dmp.utils.NoiseProperties

import java.util
import scala.collection.JavaConverters._

/**
 * Options:
 * --------
 * batchSize - кол-во обрабатываемых файлов в microBatch
 * startingOffset - явно задаваемый момент времени (timestamp) либо <earliest> либо <latest>
 * file.delete.after - удалить файл после загрузки (true), по-умолчанию значение false
 *
 * file.protective.delay.ms - временной лаг в миллисекундах, определяющий отставание загрузчика от
 * текущего момента времени, позволяет исключить ошибки,
 * связанные с отклонением системных часов
 *
 * sftp.host - хост sftp-сервера
 * sftp.port - порт sftp-сервера, по-умолчанию 22
 * sftp.root - директория в которой размещаются файлы для загрузки
 *
 * sftp.user - учетные даннные для аутентификации пользователя на sftp-сервере
 * sftp.password -
 *
 * sftp.including.files - regexp-выражение для фильтрации файлов по именам,
 * включающая их в список в случае совпадения. Например:
 * ^(SMSR_BIGDATA_)(.*)([0-9]{4})(.txt)$
 * ^SMSRouter(.*)([0-9]{4})$
 *
 * sftp.excluding.files - обратная предыдущей функция, regexp-выражение для фильтрации файлов по именам, исключающая их
 * из списка в случае совпадения. Эксперименталная(Experimental).
 */


class NoiseMicroBatchStream(val schema: StructType,
                            val properties: util.Map[String, String],
                            val options: CaseInsensitiveStringMap,
                            checkpointLocation: String,

                           )
  extends MicroBatchStream
    with Logging
    with SupportsAdmissionControl {


  private val parameters: Map[String, String] = properties.asScala.toMap

  /**
   * критическое количество ошибок, связанных с подключением к sftp-серверам
   * Должно быть больше 0.
   */
  private val CRIRTICAL_NUM_ERROR: Int = parameters.getOrElse("sftp-crirtical-num-error", "-1").toInt

  /**
   * время старта приложения
   */
  private val startTime: Long = System.currentTimeMillis()

  /**
   * Аккумулятор списока ошибок, связанных с подключением к sftp-серверам
   */
  private val sizeTotal: LongAccumulator = new LongAccumulator()
  private val rowsTotal: LongAccumulator = new LongAccumulator()
  private val partTotal: LongAccumulator = new LongAccumulator()
  private val ID: LongAccumulator = new LongAccumulator()

  val spark = SparkSession.getActiveSession.get
  spark.sparkContext.register(sizeTotal, "sizeTotal")
  spark.sparkContext.register(rowsTotal, "rowsTotal")
  spark.sparkContext.register(partTotal, "partTotal")
  spark.sparkContext.register(ID, "ID")

  /**
   * Список sftp-серверов
   */
  private val noiseProp: NoiseProperties = NoiseProperties.apply(properties)


  override def planInputPartitions(start: Offset, end: Offset): Array[InputPartition] = {
    (0 until noiseProp.partNum).map { file => new NoiseInputPartition(noiseProp) }.toArray
  }


  override def createReaderFactory: PartitionReaderFactory =
    new NoisePartitionReaderFactory(schema, sizeTotal, rowsTotal, partTotal)


  override def initialOffset: Offset = {
    val offsets = getOrCreateInitialPartitionOffsets(noiseProp)
    logInfo(s"initialOffset: '${offsets.json()}'")
    offsets
  }

  override def deserializeOffset(json: String): Offset = {
    logInfo(s"deserializeOffset: ${json}")
    NoiseSourceOffsetPilot.create(json)
  }

  override def commit(end: Offset): Unit = {
    logInfo(s"commit: ${end}")
  }

  override def stop(): Unit = {}


  override def latestOffset(): Offset = {
    throw new UnsupportedOperationException(
      "latestOffset(Offset, ReadLimit) should be called instead of this method")
  }

  override def latestOffset(start: Offset, limit: ReadLimit): Offset = {

    ID.add(1l)
    val _offsets: Seq[(String, Long)] = Seq((noiseProp.key, ID.value))
    val offsets = NoiseSourceOffsetPilot.create(_offsets)
    logInfo(s">>> >>> >>> Current OFFSETS = ${offsets.json()}")
    offsets

  }

  /**
   * Сritical number of errors check
   * Проверяет текущее число ошибок. Сравнивает в допустимым значением.
   * Если число ошибок превышено вызывает исключение.
   *
   * @return Boolean
   */


  private def getOrCreateInitialPartitionOffsets(noiseProp: NoiseProperties): NoiseSourceOffsetPilot = {

    // SparkSession is required for getting Hadoop configuration for writing to checkpoints
    assert(SparkSession.getActiveSession.nonEmpty)

    val metadataLog =
      new NoiseSourceInitialOffsetWriter(SparkSession.getActiveSession.get, checkpointLocation)

    metadataLog.get(0) match {
      case Some(value) => value
      case None =>
        val _offsets: Seq[(String, Long)] = Seq((noiseProp.key, 0l))
        val offsets: NoiseSourceOffsetPilot = NoiseSourceOffsetPilot.create(_offsets)
        offsets

    }

  }

  def getOffsetValue(noiseProp: NoiseProperties): Long = {
    val currentTime: Long = LandmarkTimestamp.getLatestTimestamp(protectiveDelay = 0)
    currentTime
  }

}