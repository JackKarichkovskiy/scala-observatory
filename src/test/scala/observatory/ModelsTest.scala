package observatory

import org.junit.Assert
import org.scalatest.funsuite.AnyFunSuite

class ModelsTest extends AnyFunSuite {

  test("check greatCircleDistance result for antipode points") {
    val loc1 = Location(0, 0)
    val loc2 = Location(0, 180)

    val result = loc1.greatCircleDistance(loc2)

    assert(result - 20015 < 1)
    assert(result - 20015 > 0)
  }


  test("check greatCircleDistance result for equal points") {
    val loc1 = Location(90, 90)
    val loc2 = Location(90, 90)

    val result = loc1.greatCircleDistance(loc2)

    assert(result == 0)
  }

  test("check getGridSquare result for positive coordinates") {
    val loc = Location(20.3, 43.7)

    val result = loc.getGridSquare()

    Assert.assertEquals(GridLocation(21, 43), result._1)
    Assert.assertEquals(GridLocation(20, 44), result._2)
  }

  test("check getGridSquare result for negative coordinates") {
    val loc = Location(-20.3, -43.7)

    val result = loc.getGridSquare()

    Assert.assertEquals(GridLocation(-20, -44), result._1)
    Assert.assertEquals(GridLocation(-21, -43), result._2)
  }

  test("check getGridSquare result for min coordinates") {
    val loc = Location(-90, -180)

    val result = loc.getGridSquare()

    Assert.assertEquals(GridLocation(-89, -180), result._1)
    Assert.assertEquals(GridLocation(90, -179), result._2)
  }

  test("check getGridSquare result for max coordinates") {
    val loc = Location(90, 180)

    val result = loc.getGridSquare()

    Assert.assertEquals(GridLocation(90, 179), result._1)
    Assert.assertEquals(GridLocation(89, -180), result._2)
  }

}
