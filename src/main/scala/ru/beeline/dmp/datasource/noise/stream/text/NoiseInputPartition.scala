package ru.beeline.dmp.datasource.noise.stream.text

import org.apache.spark.internal.Logging
import org.apache.spark.sql.connector.read.InputPartition
import ru.beeline.dmp.utils.NoiseProperties

class NoiseInputPartition(val noiseProp: NoiseProperties)

  extends InputPartition with Logging {
  override def preferredLocations = new Array[String](0) // No preferred location

}

