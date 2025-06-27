package ru.beeline.dmp.datasource.noise.batch.text

import org.apache.spark.sql.connector.catalog.{SupportsRead, Table, TableCapability}
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import ru.beeline.dmp.utils.NoiseProperties

import scala.collection.JavaConverters._

class NoiseBatchTable(val np: NoiseProperties) extends Table with SupportsRead {

  override def name(): String = this.getClass.toString

  override def schema(): StructType = StructType(Array(StructField("value", StringType)))

  override def capabilities(): java.util.Set[TableCapability] = Set(TableCapability.BATCH_READ).asJava

  override def newScanBuilder(options: CaseInsensitiveStringMap): ScanBuilder = new NoiseScanBuilder(np)

}
