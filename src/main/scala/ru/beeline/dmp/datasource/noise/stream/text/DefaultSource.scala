package ru.beeline.dmp.datasource.noise.stream.text

import org.apache.spark.internal.Logging
import org.apache.spark.sql.connector.catalog.{Table, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.{DataTypes, Metadata, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import ru.beeline.dmp.datasource.noise.stream.text.DefaultSource.getSchema

import java.util

class DefaultSource() extends TableProvider with Logging  {
  override def inferSchema(options: CaseInsensitiveStringMap): StructType = getSchema

  override def getTable(
                         schema: StructType,
                         partitioning: Array[Transform],
                         properties: util.Map[String, String]): Table = {
    new NoiseTable(schema, properties)
  }

  override def supportsExternalMetadata = true
}


object DefaultSource {
  def getSchema: StructType = {

    val structFields: Array[StructField] = Array[StructField](
      StructField("rownum", DataTypes.IntegerType, true, Metadata.empty),
      StructField("partition", DataTypes.IntegerType, true, Metadata.empty),
      StructField("timestamp", DataTypes.LongType, true, Metadata.empty),
      StructField("value", DataTypes.StringType, true, Metadata.empty),
    )

    new StructType(structFields)
  }
}
