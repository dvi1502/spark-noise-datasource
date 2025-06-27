package ru.beeline.dmp.datasource.noise.batch.text

import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReader, PartitionReaderFactory}
import ru.beeline.dmp.utils.NoiseProperties


// simple class to organise the partition
class NoisePartition(val noiseProp: NoiseProperties) extends InputPartition

// reader factory
class NoisePartitionReaderFactory extends PartitionReaderFactory with Logging {

  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {

    logDebug(s">>> >>> SimplePartitionReaderFactory.createReader = ${partition.asInstanceOf[NoisePartition].noiseProp.key.toString()}")
    new NoisePartitionReader(partition.asInstanceOf[NoisePartition])

  }

}

