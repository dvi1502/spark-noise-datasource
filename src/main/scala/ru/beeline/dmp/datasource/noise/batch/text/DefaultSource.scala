package ru.beeline.dmp.datasource.noise.batch.text

import org.apache.spark.sql.connector.catalog.{Table, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import ru.beeline.dmp.utils.NoiseProperties

class DefaultSource extends TableProvider {

  override def inferSchema(caseInsensitiveStringMap: CaseInsensitiveStringMap): StructType =
    getTable(null, Array.empty[Transform], caseInsensitiveStringMap.asCaseSensitiveMap()).schema()

  override def getTable(structType: StructType, transforms: Array[Transform], _parameters: java.util.Map[String, String]): Table = {

    val sap = NoiseProperties.apply(_parameters)
    new NoiseBatchTable(sap)

  }
}





