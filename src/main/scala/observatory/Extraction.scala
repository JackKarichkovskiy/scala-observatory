package observatory

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, Row}

import java.time.LocalDate
import scala.io.Source

/**
  * 1st milestone: data extraction
  */
object Extraction extends ExtractionInterface {

  import SparkInstance.spark.implicits._

  /**
    * Load the resource from filesystem as Dataset[String]
    * @param resource the resource path
    * @return the resource content as Dataset[String]
    */
  def getRddFromResource(resource: String): RDD[String] = {
    println("getRddFromResource start")
    val fileStream = Source.getClass.getResourceAsStream(resource)
    SparkInstance.sparkContext.makeRDD(Source.fromInputStream(fileStream).getLines().toList)
  }
  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    println("locateTemperatures start")

    locateTemperaturesDS(year, stationsFile, temperaturesFile)
  }

  def locateTemperaturesDS(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    println("locateTemperaturesDS start")

    val stations = SparkInstance.spark.read.csv(getRddFromResource(stationsFile).toDS())
      .filter(row => !row.isNullAt(2) && !row.isNullAt(3))
      .map(rowToStation)

    val temperatures = SparkInstance.spark.read.csv(getRddFromResource(temperaturesFile).toDS())
      .map(rowToTemperature)

    stations.join(temperatures, "key").rdd
      .map(rowToLocaleTemperature(year, _))
      .collect()
  }

  def locateTemperaturesRdd(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    println("locateTemperaturesRdd start")

    val stationsByKey = parseStations(getRddFromResource(stationsFile))
      .map(station => (station.key, station))

    val temperatures = parseTemperatures(getRddFromResource(temperaturesFile))
      .map(temp => (temp.key, temp))

    stationsByKey.join(temperatures)
      .map(rowToLocaleTemperature(year, _))
      .collect()
  }

  def parseStations(stationsRdd: RDD[String]): RDD[Station] = {
    stationsRdd
      .map(line => line.split(",", -1))
      .filter(arr => Option(arr(2)).exists(_.trim.nonEmpty) && Option(arr(3)).exists(_.trim.nonEmpty))
      .map(arr => Station(
        StationKey(arr(0), arr(1)),
        Location(arr(2).toDouble, arr(3).toDouble)
      ))
  }

  def rowToStation(row: Row): Station = {
    Station(
      StationKey(row.getString(0), row.getString(1)),
      Location(row.getString(2).toDouble, row.getString(3).toDouble)
    )
  }

  def parseTemperatures(stationsRdd: RDD[String]): RDD[TemperatureRow] = {
    stationsRdd.map(_.split(",", -1))
      .map(arr => TemperatureRow(
        StationKey(arr(0), arr(1)),
        arr(2).toInt, arr(3).toInt,
        fahrenheitToCelsius(arr(4).toDouble)
      ))
  }

  def rowToTemperature(row: Row): TemperatureRow = {
    TemperatureRow(
      StationKey(row.getString(0), row.getString(1)),
      row.getString(2).toInt, row.getString(3).toInt,
      fahrenheitToCelsius(row.getString(4).toDouble)
    )
  }

  def fahrenheitToCelsius(degrees: Double): Temperature = {
    (degrees - 32) * 5.0 / 9
  }

  def rowToLocaleTemperature(year: Year, row: Row): (LocalDate, Location, Temperature) = {
    (
      LocalDate.of(year, row.getInt(2), row.getInt(3)),
      Location(row.getStruct(1).getDouble(0), row.getStruct(1).getDouble(1)),
      row.getDouble(4)
    )
  }

  def rowToLocaleTemperature(year: Year, row: (StationKey, (Station, TemperatureRow))): (LocalDate, Location, Temperature) = {
    val (station, temperature) = row._2
    (
      LocalDate.of(year, temperature.month, temperature.day),
      Location(station.loc.lat, station.loc.lon),
      temperature.temp
    )
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    println("locationYearlyAverageRecords start")

    SparkInstance.sparkContext.parallelize(records.toSeq)
      .map(tuple => (tuple._2, tuple._3))
      .groupByKey()
      .aggregateByKey((0.0, 0))(
        (acc, v) => (acc._1 + v.sum, acc._2 + v.size),
        (v1, v2) => (v1._1 + v2._1, v1._2 + v2._2)
      )
      .mapValues(sum => sum._1 / sum._2)
      .collect()
  }

}
