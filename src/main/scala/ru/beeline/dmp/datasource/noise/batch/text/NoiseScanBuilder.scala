package ru.beeline.dmp.datasource.noise.batch.text

import org.apache.spark.sql.connector.read.{Scan, ScanBuilder}
import ru.beeline.dmp.utils.NoiseProperties

/*
   Scan object with no mixins
 */
class NoiseScanBuilder(val np: NoiseProperties) extends ScanBuilder {

  override def build(): Scan = new NoiseScan(np)

}
