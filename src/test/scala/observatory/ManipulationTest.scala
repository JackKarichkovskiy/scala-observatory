package observatory

import observatory.Manipulation._
import org.junit.Assert.assertEquals
import org.junit.Test

import java.time.LocalDate

trait ManipulationTest extends MilestoneSuite {
  private val milestoneTest = namedMilestoneTest("data manipulation", 4) _

  // Implement tests for methods of the `Manipulation` object

  @Test def `test makeGrid method`: Unit = milestoneTest {
    val samples = List(
      (Location(90, 90), 25.0),
      (Location(0, 180), 50.0)
    )
    val gridLocation = GridLocation(0, 0)

    val resultFun = makeGrid(samples)

    assertEquals(25.385, resultFun(gridLocation), 0.001)
    assertEquals(25.385, resultFun(gridLocation), 0.001)
  }

  @Test def `test average method`: Unit = milestoneTest {
    val input = List(
      List((Location(3, 3), 20.0)),
      List((Location(1, 1), 10.0), (Location(3, 3), 40.0))
    )

    val resultFun = average(input)

    assertEquals(15.0, resultFun(GridLocation(1, 1)), 0)
    assertEquals(22.5, resultFun(GridLocation(2, 2)), 0.01)
    assertEquals(30.0, resultFun(GridLocation(3, 3)), 0)
  }

  @Test def `test deviation method`: Unit = milestoneTest {
    val temperatures = List(
      (Location(1, 1), 20.0),
      (Location(3, 3), 10.0)
    )

    val resultFun = deviation(temperatures, _ => 10.0)


    assertEquals(10.0, resultFun(GridLocation(1, 1)), 0)
    assertEquals(0.0, resultFun(GridLocation(3, 3)), 0)
    assertEquals(5.0, resultFun(GridLocation(2, 2)), 0.1)
  }

}
