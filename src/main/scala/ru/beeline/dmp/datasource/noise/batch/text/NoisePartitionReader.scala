package ru.beeline.dmp.datasource.noise.batch.text

import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.unsafe.types.UTF8String
import ru.beeline.dmp.utils.AnyFieldProducer
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

// parathion reader
class NoisePartitionReader(inputPartition: NoisePartition) extends PartitionReader[InternalRow] with Logging {

  logInfo(s">>> >>> Loaded file '${inputPartition.noiseProp.key.toString()}'")

  val np = inputPartition.noiseProp
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  val downloadTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).format(formatter)

  val values = (0 until np.partSize).map(id => AnyFieldProducer.genvalue(np.pattern))

  var index = 0

  def next: Boolean = index < inputPartition.noiseProp.partSize

  def get: InternalRow = {
    try {
      InternalRow( UTF8String.fromString(values(index)))
    }
    finally {
      index += 1
    }
  }

  def close(): Unit = {
    //
  }

}