package ru.beeline.dmp.datasource.noise.stream.text

import org.apache.spark.internal.Logging
import org.apache.spark.sql.connector.read.Scan
import org.apache.spark.sql.connector.read.streaming.MicroBatchStream
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util

class NoiseScan(val schema: StructType, val properties: util.Map[String, String], val options: CaseInsensitiveStringMap) extends Scan with Logging  {
  override def readSchema: StructType = schema

  override def description: String = "sftp_dir_scan"

  override def toMicroBatchStream(checkpointLocation: String): MicroBatchStream =
    new NoiseMicroBatchStream(schema, properties, options, checkpointLocation)
}
