package observatory

import observatory.Extraction.{locateTemperatures, locationYearlyAverageRecords}
import observatory.Interaction.{generateTiles, tile}
import com.sksamuel.scrimage.writer
import observatory.Manipulation.average
import org.apache.spark.sql.SparkSession

object Main extends App {

  def profile[T](blockName: String)(block: => T): T = {
    val startTime = System.currentTimeMillis()
    println(s"$blockName started")

    val result = block

    val spentTime = System.currentTimeMillis() - startTime
    println(s"$blockName finished in $spentTime ms")

    result
  }

  val startTime = System.currentTimeMillis()
  println(s"Time: $startTime ms - Main started")


  val deviationColorsPalette = List(
    (7.0, Color(0, 0, 0)),
    (4.0, Color(255, 0, 0)),
    (2.0, Color(255, 255, 0)),
    (0.0, Color(255, 255, 255)),
    (-2.0, Color(0, 255, 255)),
    (-7.0, Color(0, 0, 255))
  )

  def generateAllTemperatureTiles(): Unit = {
    val temperatureColorsPalette = List(
      (60.0, Color(255, 255, 255)),
      (32.0, Color(255, 0, 0)),
      (12.0, Color(255, 255, 0)),
      (0.0, Color(0, 255, 255)),
      (-15.0, Color(0, 0, 255)),
      (-27.0, Color(255, 0, 255)),
      (-50.0, Color(33, 0, 107)),
      (-60.0, Color(0, 0, 0)),
    )

    val year = 2013
    val temperatures = locateTemperatures(year, "/stations.csv", s"/$year.csv")
    val averageTemperatures = locationYearlyAverageRecords(temperatures)

    def generateImage(year: Year, tileObj: Tile, data: Iterable[(Location, Temperature)]): Unit = {
      val tileFilePath = s"target/temperatures/$year/${tileObj.zoom}/${tileObj.x}-${tileObj.y}.png"
      println("File to write: " + tileFilePath)
      val targetFile = new java.io.File(tileFilePath)
      targetFile.getParentFile.mkdirs()
      tile(data, temperatureColorsPalette, tileObj).output(targetFile)
    }

    generateTiles(List((year, averageTemperatures)), generateImage)
  }

//  generateAllTemperatureTiles()

  val fromYear = 1975
  val toYear = 1985

  val averageTemperaturesByYear = profile("extraction calculation") {
    (fromYear until toYear).par
      .map(year => locateTemperatures(year, "/stations.csv", s"/$year.csv"))
      .map(locationYearlyAverageRecords)
  }

  val zeroTemp = profile("average calculation") {
    val averageGrid = average(averageTemperaturesByYear.seq)
    averageGrid(GridLocation(0, 0))
  }


  val timeTaken = System.currentTimeMillis() - startTime
  println(s"Time taken: $timeTaken ms - Main finished")
}

