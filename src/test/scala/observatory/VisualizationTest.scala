package observatory

import com.sksamuel.scrimage.writer
import observatory.Extraction.{locateTemperatures, locationYearlyAverageRecords}
import observatory.Visualization._
import org.junit.Assert._
import org.junit.Test

trait VisualizationTest extends MilestoneSuite with TestData {

  private val milestoneTest = namedMilestoneTest("raw data display", 2) _

  // Implement tests for the methods of the `Visualization` object

  @Test def `test predictTemperature method`: Unit = milestoneTest {
    val samples = List(
      (Location(90, 90), 20.0),
      (Location(90, 80), 50.0)
    )
    val location = Location(90, 85)

    val result = predictTemperature(samples, location)

    assertEquals(35.0, result, 0.0)
  }

  @Test def `test predictTemperature method if target Location is in samples`: Unit = milestoneTest {
    val samples = List(
      (Location(90, 90), 25.0),
      (Location(0, 180), 50.0)
    )
    val location = Location(90, 90)

    val result = predictTemperature(samples, location)

    assertEquals(25.0, result, 0)
  }

  @Test def `test interpolateColor method - lowest`: Unit = milestoneTest {
    val samplePoints = List(
      (40.0, Color(40, 0, 0)),
      (20.0, Color(20, 0, 0)),
      (25.0, Color(25, 0, 0)),
      (10.0, Color(10, 0, 0)),
      (5.0, Color(5, 0, 0))
    )

    val result = interpolateColor(samplePoints, 0)

    assert(result == Color(5, 0, 0))
  }

  @Test def `test interpolateColor method - exact highest boundary`: Unit = milestoneTest {
    val samplePoints = List(
      (40.0, Color(40, 0, 0)),
      (20.0, Color(20, 0, 0)),
      (25.0, Color(25, 0, 0)),
      (10.0, Color(10, 0, 0)),
      (5.0, Color(5, 0, 0))
    )

    val result = interpolateColor(samplePoints, 40)

    assert(result == Color(40, 0, 0))
  }

  @Test def `test interpolateColor method - highest`: Unit = milestoneTest {
    val samplePoints = List(
      (40.0, Color(40, 0, 0)),
      (20.0, Color(20, 0, 0)),
      (25.0, Color(25, 0, 0)),
      (10.0, Color(10, 0, 0)),
      (5.0, Color(5, 0, 0))
    )

    val result = interpolateColor(samplePoints, 50)

    assert(result == Color(40, 0, 0))
  }

  @Test def `test interpolateColor method - middle`: Unit = milestoneTest {
    val samplePoints = List(
      (40.0, Color(40, 0, 0)),
      (20.0, Color(20, 10, 0)),
      (25.0, Color(25, 0, 0)),
      (10.0, Color(10, 30, 0)),
      (5.0, Color(5, 0, 0))
    )

    val result = interpolateColor(samplePoints, 15)

    assert(result == Color(15, 20, 0))
  }

//  @Test def `test visualize`: Unit = milestoneTest {
//    val temperatures = locateTemperatures(2015, "/stations.csv", "/2015.csv")
//    val averageTemperatures = locationYearlyAverageRecords(temperatures)
//
//    val result = profile("visualizeOld") {
//      visualizeOld(averageTemperatures, colorsPalette)
//    }
//    result.output(new java.io.File("target/temperatures-2015-old.png"))
//    assert(result != null)
//
//    val result2 = profile("visualize") {
//      visualize(averageTemperatures, colorsPalette)
//    }
//    result2.output(new java.io.File("target/temperatures-2015.png"))
//    assert(result2 != null)
//  }

  def profile[T](blockName: String)(block: => T): T = {
    val startTime = System.currentTimeMillis()
    println(s"$blockName started")

    val result = block

    val spentTime = System.currentTimeMillis() - startTime
    println(s"$blockName finished in $spentTime ms")

    result
  }
}
