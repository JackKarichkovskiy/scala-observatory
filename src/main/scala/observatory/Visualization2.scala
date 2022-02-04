package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Interaction.{imageSize, tileLocation, tilePixelScale, zoomTile, toPixel}
import observatory.Visualization.{interpolateColor, predictTemperature}

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 extends Visualization2Interface {

  private val tilePixelScale = 8
  private val imageSize = 256

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature,
    d01: Temperature,
    d10: Temperature,
    d11: Temperature
  ): Temperature = {
    val x = point.x
    val y = point.y
    d00 * (1 - x) * (1 - y) + d10 * x * (1 - y) + d01 * (1 - x) * y + d11 * x * y
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
    grid: GridLocation => Temperature,
    colors: Iterable[(Temperature, Color)],
    tile: Tile
  ): Image = {
    val startTime = System.currentTimeMillis()
    println(s"Time: $startTime ms - tile generation start: $tile")

    val (leftTopTile, rightBottomTile) = zoomTile(tile, tilePixelScale)
    val zoomedTiles = for (y <- leftTopTile.y to rightBottomTile.y;
                           x <- leftTopTile.x to rightBottomTile.x)
    yield Tile(x, y, leftTopTile.zoom)

    def createPixel(targetLoc: Location): Pixel = {
      val (topLeft, bottomRight) = targetLoc.getGridSquare()
      val predictedTemperature = bilinearInterpolation(
        CellPoint(targetLoc.lon - topLeft.lon,  topLeft.lat - targetLoc.lat),
        grid(topLeft), grid(GridLocation(bottomRight.lat, topLeft.lon)),
        grid(GridLocation(topLeft.lat, bottomRight.lon)), grid(bottomRight)
      )
      val interpolatedColor = interpolateColor(colors, predictedTemperature)
      toPixel(interpolatedColor, 127)
    }

    val pixels = zoomedTiles.par.map(tileLocation).map(createPixel).toArray
    val image = Image(imageSize, imageSize, pixels)

    val timeTaken = System.currentTimeMillis() - startTime
    println(s"Time taken: $timeTaken ms - tile generation end: $tile")

    image
  }

}
