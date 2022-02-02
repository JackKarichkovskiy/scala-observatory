package observatory

import observatory.Extraction.{locateTemperatures, locationYearlyAverageRecords}
import observatory.Manipulation.{average, deviation}
import observatory.Visualization2._
import com.sksamuel.scrimage.writer
import org.junit.Assert.assertEquals
import org.junit.Test

trait Visualization2Test extends MilestoneSuite with TestData {
  private val milestoneTest = namedMilestoneTest("value-added information visualization", 5) _

  // Implement tests for methods of the `Visualization2` object

  @Test def `test bilinearInterpolation method`: Unit = milestoneTest {
    val result = bilinearInterpolation(CellPoint(0.1, 0), 0, 0, -1.1920928955078125E-5, 0)

    assertEquals(-0.00000119209289550781250, result, 0.0000001)
  }

  @Test def `test visualizeGrid method`: Unit = milestoneTest {
    val fromYear = 1975
    val toYear = 1977

    val averageTemperaturesByYear = (fromYear until toYear).par
      .map(year => locateTemperatures(year, "/stations.csv", s"/$year.csv"))
      .map(locationYearlyAverageRecords)
    val normals = average(averageTemperaturesByYear.seq)

    val temperatures = locateTemperatures(2015, "/stations.csv", "/2015.csv")
    val averageTemperatures = locationYearlyAverageRecords(temperatures)

    val deviationGrid = deviation(averageTemperatures, normals)


    // WHEN
    val result = visualizeGrid(deviationGrid, deviationColorsPalette, Tile(0, 0, 0))

    // THEN
    result.output(new java.io.File("target/deviations-2015.png"))
    assert(result != null)
  }

}
