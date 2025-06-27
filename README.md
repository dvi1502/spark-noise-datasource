# SPARK NOISE DATASOURCE (api v.2)

В библиотеке реализован Spark Datasource api v.2 для имитации потока данных по заданному пользователем шаблону.

## Как собрать и опубликовать проект в локальном репозитории

```bash
sbt clean; sbt  reload; sbt publishLocal
```


## Как подключить библиотеку к своему проекту
```scala

resolvers += ivyLocal

libraryDependencies += "ru.beeline.dmp" % "spark-noise-datasource_2.12" % "0.0.1-SNAPSHOT"

````

## Как применить на практике
### ... в режиме BATCH 
Возвращает заполненый датафрейм количеством партиций **partNum**, размером партиции **partSize** и структурой **pattern**.
Пример:
```scala
val df = spark.read
  .format("ru.beeline.dmp.datasource.noise.batch.text")
  .option("key", java.util.UUID.randomUUID.toString)
  .option("partSize", "3000")
  .option("partNum", "7")
  .option("pattern", "7937{NUM(7)};{ENG(25,2)};{NUM(8)};{TS};{DT};1251{HEX(8,1)};{RUS(15,0)}") 
  .load()
```

### ... в режиме STREAM
Возвращает заполненый поток данных, в котором MicroBatch количеством партиций **partNum**, размером партиции **partSize** и структурой **pattern**.
Пример:
```scala
val stream = spark.readStream
  .format("ru.beeline.dmp.datasource.noise.stream.text")
  .option("key", java.util.UUID.randomUUID.toString)
  .option("partSize", "1000")
  .option("partNum", "5")
  .option("pattern", "7937{NUM(7)};{ENG(25,2)};{NUM(8)};{TS};{DT};1251{HEX(8,1)};{RUS(15,0)}")
  .load
```



## Функции, реализованные в шаблоне **pattern**:

- {NUM(7,2)} - подставляет число, состоящее в формате ЧЧЧЧЧЧЧ.ЧЧ
- {ENG(25,2)} - подставляет строку, состоящую из символов a-z, где 25 - длина строки, 2 - регистр символов (0-lower, 1-upper, 2-camel)
- {RUS(15,0)} - подставляет строку, состоящую из символов а-я, где 15 - длина строки, 0 - регистр (0-lower, 1-upper, 2-camel)
- {TS} - подставляет текущие дату и время в стандарте ISO8601 (YYYY-MM-DDTHH:mm:ss)
- {DT} - подставляет текущие дату в стандарте ISO8601 (YYYY-MM-DD)
- {HEX(8,1)} - подставляет 8 символов из набора 0-9 + A-F, второе значение задает регистр 0 - lowercase, 1 - uppercase



            
