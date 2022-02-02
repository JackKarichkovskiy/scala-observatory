package observatory

import org.apache.spark.rdd.RDD
import SparkInstance.sparkContext

import scala.collection.parallel.ParIterable

/**
  * 4th milestone: value-added information
  */
object Manipulation extends ManipulationInterface {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    System.gc()
    val cache = collection.mutable.Map.empty[GridLocation, Temperature]
    gridLoc =>
      cache.getOrElse(gridLoc, {
        val temp = Visualization.predictTemperature(temperatures, gridLoc.toLocation())
        cache update(gridLoc, temp)
        cache(gridLoc)
      })
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
//    System.gc()
//    averageRdd(sparkContext.parallelize(temperatures.toSeq))
    ???
  }

  def averageRdd(temperaturesRdd: RDD[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    gridLoc => {
      val sum = temperaturesRdd.map(makeGrid).map(_(gridLoc)).aggregate((0.0, 0))(
        (acc, temp) => (acc._1 + temp, acc._2 + 1),
        (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2)
      )
      sum._1 / sum._2
    }
  }

  def averageRdd2(temperaturesRdd: RDD[Iterable[(Location, Temperature)]]): Iterable[(Location, Temperature)] = {
    temperaturesRdd
      .flatMap(locWithTemp => locWithTemp)
      .groupByKey()
      .aggregateByKey((0.0, 0))(
        (acc, v) => (acc._1 + v.sum, acc._2 + v.size),
        (v1, v2) => (v1._1 + v2._1, v1._2 + v2._2)
      )
      .mapValues(sum => sum._1 / sum._2)
      .collect()
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
//    System.gc()
//    gridLoc => makeGrid(temperatures)(gridLoc) - normals(gridLoc)
    ???
  }


}

