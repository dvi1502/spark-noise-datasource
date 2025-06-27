package ru.beeline.dmp.datasource.noise.stream.text

import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.types.StructType
import org.apache.spark.unsafe.types.UTF8String
import org.apache.spark.util.LongAccumulator
import ru.beeline.dmp.utils.AnyFieldProducer

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

class NoisePartitionReader(val inputPartition: NoiseInputPartition, val schema: StructType,
                           sizeTotal: LongAccumulator, rowsTotal: LongAccumulator, filesTotal: LongAccumulator,
                          )
  extends PartitionReader[InternalRow] with Logging {

  val np = inputPartition.noiseProp

  val lines: List[String] = {
    (0 until np.partSize).map { id => AnyFieldProducer.genvalue(np.pattern) }.toList
  }


  var counter = 0

  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  val downloadTime = {
    //  val dt = LocalDateTime.now().atZone(ZoneId.systemDefault()).format(formatter)
    LocalDateTime.now()
      .atZone(ZoneId.systemDefault())
      .toInstant
      .toEpochMilli
  }

  override def next: Boolean = counter < lines.length


  override def get: InternalRow = {
    try {
      InternalRow.fromSeq(List(
        counter,
        np.partNum,
        downloadTime,
        UTF8String.fromString(lines(counter)),
      ))
    } finally {
      counter += 1
    }
  }


  override def close(): Unit = {
  }


}

