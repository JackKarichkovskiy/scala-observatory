package observatory

import org.apache.spark.rdd.RDD

/**
  * 4th milestone: value-added information
  */
object Manipulation extends ManipulationInterface {

  var cache = Map.empty[Iterable[(Location, Temperature)], Map[GridLocation, Temperature]]

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    val cachedGrid = cache.getOrElse(temperatures, {
      val newGrid = generateGrid(temperatures)
      cache += temperatures -> newGrid
      newGrid
    })
    gridLoc => {
      cachedGrid(gridLoc)
    }
  }

  def generateGrid(temperatures: Iterable[(Location, Temperature)]): Map[GridLocation, Temperature] = {
    {
      for {
        lat <- -89 to 90
        lon <- -180 to 179
      } yield GridLocation(lat, lon) -> Visualization.predictTemperature(temperatures, Location(lat, lon))
    }.toMap
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    averagePar(temperatures)
  }

  def averagePar(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    val gridsByYear = temperatures.map(makeGrid)
    println("gridsByYear created")

    gridLoc => {
      val sum = gridsByYear.par.map(_(gridLoc)).aggregate((0.0, 0))(
        (acc, temp) => (acc._1 + temp, acc._2 + 1),
        (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2)
      )
      sum._1 / sum._2
    }
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

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val selectedYearGrid = makeGrid(temperatures)
    gridLoc => selectedYearGrid(gridLoc) - normals(gridLoc)
  }


}

