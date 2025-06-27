package ru.beeline.dmp.datasource.noise.stream.text

import org.apache.spark.internal.Logging
import org.apache.spark.sql.connector.read.{Scan, ScanBuilder}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util

class NoiseScanBuilder(val schema: StructType, val properties: util.Map[String, String], val options: CaseInsensitiveStringMap)
  extends ScanBuilder with Logging  {
  override def build: Scan = new NoiseScan(schema, properties, options)
}

