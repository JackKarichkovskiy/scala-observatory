package observatory

import observatory.Extraction._
import org.junit.Assert._
import org.junit.Test

import java.time.LocalDate


trait ExtractionTest extends MilestoneSuite {
  private val milestoneTest = namedMilestoneTest("data extraction", 1) _

  // Implement tests for the methods of the `Extraction` object

  @Test def `test locateTemperatures method`: Unit = milestoneTest {
    val result = locateTemperatures(1975, "/stations.csv", "/1975.csv")

    assertEquals(2177190, result.size)
  }

  @Test def `test locationYearlyAverageRecords method`: Unit = milestoneTest {
    val input = List(
      (LocalDate.of(1995, 12, 22), Location(1, 1), 20.0),
      (LocalDate.of(1995, 12, 22), Location(2, 2), 10.0),
      (LocalDate.of(1995, 12, 23), Location(1, 1), 40.0),
    )

    val result = locationYearlyAverageRecords(input)

    val expected = List(
      (Location(1, 1), 30.0),
      (Location(2, 2), 10.0)
    )
    assertEquals(expected, result)
  }
}
