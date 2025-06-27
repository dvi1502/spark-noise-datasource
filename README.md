# SPARK NOISE DATASOURCE (api v.2)

В библиотеке реализован Spark Datasource api v.2 для имитации потока данных по заданному пользователем шаблону.

## Сборка и публикация проекта в локальном репозитории

```bash
sbt clean; sbt  reload; sbt publishLocal
```


## Подключить библиотеку к проекту

```scala
libraryDependencies += "ru.beeline.dmp" % "spark-noise-datasource_2.12" % "2.0.11"
```


## Использование библиотеки
```scala

resolvers += ivyLocal

libraryDependencies += "ru.beeline.dmp" % "spark-noise-datasource_2.12" % "0.0.1-SNAPSHOT"

````

## Режим BATCH 
возвращает заполненый датафрейм размером **size** и структурой **pattern**
```scala
val df = spark.read
  .format("ru.beeline.dmp.datasource.noise.batch.text")
  .option("key", java.util.UUID.randomUUID.toString)
  .option("partSize", "3000")
  .option("partNum", "7")
  .option("pattern", "7937{NUM(7)};{ENG(25,2)};{NUM(8)};{TS};{DT};1251{HEX(8,1)};{RUS(15,0)}") 
  .load()
```

## Режим STREAM
возвращает заполненый поток данных, в котором MicroBatch размером **size** и структурой **pattern**
```scala
val stream = spark.readStream
  .format("ru.beeline.dmp.datasource.noise.stream.text")
  .option("key", java.util.UUID.randomUUID.toString)
  .option("partSize", "1000")
  .option("partNum", "5")
  .option("pattern", "7937{NUM(7)};{ENG(25,2)};{NUM(8)};{TS};{DT};1251{HEX(8,1)};{RUS(15,0)}")
  .load
```




## Функции, перечисленные в шаблоне данных:

- {NUM(7,2)} - подставляет число, состоящее в формате ЧЧЧЧЧЧЧ.ЧЧ
- {ENG(25,2)} - подставляет строку, состоящую из символов a-z, где 25 - длина строки, 2 - регистр символов (0-lower, 1-upper, 2-camel)
- {RUS(15,0)} - подставляет строку, состоящую из символов а-я, где 15 - длина строки, 0 - регистр (0-lower, 1-upper, 2-camel)
- {TS} - подставляет текущие дату и время в стандарте ISO8601 (YYYY-MM-DDTHH:mm:ss)
- {DT} - подставляет текущие дату в стандарте ISO8601 (YYYY-MM-DD)
- {HEX(8,1)} - подставляет 8 символов из набора 0-9 + A-F, второе значение задает регистр 0 - lowercase, 1 - uppercase



            
