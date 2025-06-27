package ru.beeline.dmp.datasource.noise.stream.text

import org.apache.spark.internal.Logging
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType
import org.apache.spark.util.{CollectionAccumulator, LongAccumulator}

class NoisePartitionReaderFactory(val schema: StructType,
                                  sizeTotal: LongAccumulator,
                                  rowsTotal: LongAccumulator,
                                  filesTotal: LongAccumulator,
                                )
  extends PartitionReaderFactory with Logging {

  override def createReader(partition: InputPartition) =
    new NoisePartitionReader(partition.asInstanceOf[NoiseInputPartition], schema, sizeTotal, rowsTotal, filesTotal)

}