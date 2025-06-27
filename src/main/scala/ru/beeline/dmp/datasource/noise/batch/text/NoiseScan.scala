package ru.beeline.dmp.datasource.noise.batch.text

import org.apache.spark.internal.Logging
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory, Scan}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import ru.beeline.dmp.utils.NoiseProperties

/*
    Batch Reading Support
    The schema is repeated here as it can change after column pruning etc
 */
class NoiseScan(val noiseProp: NoiseProperties) extends Scan with Batch with Logging {

  override def readSchema(): StructType = StructType(Array(StructField("value", StringType)))

  override def toBatch: Batch = this

  override def planInputPartitions(): Array[InputPartition] = {
    (0 until noiseProp.partNum).map { file => new NoisePartition(noiseProp) }.toArray
  }

  override def createReaderFactory(): PartitionReaderFactory = new NoisePartitionReaderFactory()

}