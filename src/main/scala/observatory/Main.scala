package observatory

import observatory.Extraction.{locateTemperatures, locationYearlyAverageRecords}
import observatory.Interaction.{generateTiles, tile}
import com.sksamuel.scrimage.writer
import observatory.Manipulation.{average, deviation}
import observatory.Visualization2.visualizeGrid

object Main extends App {

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

  val deviationColorsPalette = List(
    (7.0, Color(0, 0, 0)),
    (4.0, Color(255, 0, 0)),
    (2.0, Color(255, 255, 0)),
    (0.0, Color(255, 255, 255)),
    (-2.0, Color(0, 255, 255)),
    (-7.0, Color(0, 0, 255))
  )

  val startTime = System.currentTimeMillis()
  println(s"Time: $startTime ms - Main started")

  (2010 to 2012).foreach(generateAllTemperatureTiles)
//  (2010 to 2015).foreach(generateAllDeviationTiles)

  val timeTaken = System.currentTimeMillis() - startTime
  println(s"Time taken: $timeTaken ms - Main finished")

  def generateAllTemperatureTiles(selectedYear: Int): Unit = {
    val averageTemperatures = profile("average calculation") {
      val temperatures = locateTemperatures(selectedYear, "/stations.csv", s"/$selectedYear.csv")
      locationYearlyAverageRecords(temperatures)
    }

    def generateImage(year: Year, tileObj: Tile, data: Iterable[(Location, Temperature)]): Unit = {
      val tileFilePath = s"target/temperatures/$year/${tileObj.zoom}/${tileObj.x}-${tileObj.y}.png"
      println("File to write: " + tileFilePath)
      val targetFile = new java.io.File(tileFilePath)
      targetFile.getParentFile.mkdirs()
      tile(data, temperatureColorsPalette, tileObj).output(targetFile)
    }

    profile("all tiles generation") {
      generateTiles(List((selectedYear, averageTemperatures)), generateImage)
    }
  }

  def generateAllDeviationTiles(selectedYear: Int): Unit = {
    val selectedAverageTemperatures = profile("average calculation") {
      val temperatures = locateTemperatures(selectedYear, "/stations.csv", s"/$selectedYear.csv")
      locationYearlyAverageRecords(temperatures)
    }

    val fromYear = 1975
    val toYear = 1980

    val averageTemperaturesByYear = profile("extraction calculation") {
      (fromYear until toYear)
        .map(year => locateTemperatures(year, "/stations.csv", s"/$year.csv"))
        .map(locationYearlyAverageRecords)
    }

    val normals = profile("average calculation") {
      average(averageTemperaturesByYear.seq)
    }

    def generateImage(year: Year, tileObj: Tile, data: Iterable[(Location, Temperature)]): Unit = {
      val deviationGrid = profile("deviation calculation") {
        deviation(data, normals)
      }

      val tileFilePath = s"target/deviations/$year/${tileObj.zoom}/${tileObj.x}-${tileObj.y}.png"
      println("File to write: " + tileFilePath)
      val targetFile = new java.io.File(tileFilePath)
      targetFile.getParentFile.mkdirs()
      visualizeGrid(deviationGrid, deviationColorsPalette, tileObj).output(targetFile)
    }

    generateTiles(List((selectedYear, selectedAverageTemperatures)), generateImage)
  }

  def profile[T](blockName: String)(block: => T): T = {
    val startTime = System.currentTimeMillis()
    println(s"$blockName started")

    val result = block

    val spentTime = System.currentTimeMillis() - startTime
    println(s"$blockName finished in $spentTime ms")

    result
  }
}

