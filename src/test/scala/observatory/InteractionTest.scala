package observatory

import observatory.Extraction.{locateTemperatures, locationYearlyAverageRecords}
import observatory.Interaction._

import com.sksamuel.scrimage.writer
import scala.collection.concurrent.TrieMap
import org.junit.Assert._
import org.junit.Test

trait InteractionTest extends MilestoneSuite with TestData {
  private val milestoneTest = namedMilestoneTest("interactive visualization", 3) _

  @Test def `test tileLocation method - zoom 17`: Unit = milestoneTest {
    val tile = Tile(65544, 43582, 17)

    val result = tileLocation(tile)

    assertEquals(51.0, result.lat, 0)
    assertEquals(0.022, result.lon, 0.001)
    assert(result != null)
  }

  @Test def `test tileLocation method - zoom 1`: Unit = milestoneTest {
    val tile = Tile(0, 1, 1)

    val result = tileLocation(tile)

    assertEquals(0, result.lat, 0)
    assertEquals(-180, result.lon, 0.001)
    assert(result != null)
  }

//  @Test def `test tile method`: Unit = milestoneTest {
//    val temperatures = locateTemperatures(2015, "/stations.csv", "/2015.csv")
//    val averageTemperatures = locationYearlyAverageRecords(temperatures)
//    val inputTile = Tile(0, 0, 0)
//
//    val result = tile(averageTemperatures, temperatureColorsPalette, inputTile)
//
//    result.output(new java.io.File("target/interaction.png"))
//    assert(result != null)
//  }

  @Test def `test generateAllTiles method - 0 zoom`: Unit = milestoneTest {
    val result = generateAllTiles(0)

    assert(result != null)
    assertEquals(1, result.size)
  }

  @Test def `test generateAllTiles method - 3 zoom`: Unit = milestoneTest {
    val result = generateAllTiles(3)

    assert(result != null)
    assertEquals(Tile(6, 2, 3), result.asInstanceOf[Vector[Tile]](50))
    assertEquals(64, result.size)
  }

  @Test def `test generateRangeTiles method - 0 to 3 zoom`: Unit = milestoneTest {
    val result = generateRangeTiles(0, 3)

    assert(result != null)
    assertEquals(Tile(0, 0, 0), result.asInstanceOf[Vector[Tile]](0))
    assertEquals(Tile(1, 0, 1), result.asInstanceOf[Vector[Tile]](3))
    assertEquals(Tile(2, 2, 2), result.asInstanceOf[Vector[Tile]](15))
    assertEquals(Tile(6, 1, 3), result.asInstanceOf[Vector[Tile]](70))
    assertEquals(85, result.size)
  }

//  @Test def `test generateTiles method - for 2015`: Unit = milestoneTest {
//    val temperatures = locateTemperatures(2015, "/stations.csv", "/2015.csv")
//    val averageTemperatures = locationYearlyAverageRecords(temperatures)
//
//    def generateImage(year: Year, tileObj: Tile, data: Iterable[(Location, Temperature)]): Unit = {
//      val tileFilePath = s"target/temperatures/$year/${tileObj.zoom}/${tileObj.x}-${tileObj.y}.png"
//      tile(data, temperatureColorsPalette, tileObj).output(new java.io.File(tileFilePath))
//    }
//
//    generateTiles(List((2015, averageTemperatures)), generateImage)
//  }
}
