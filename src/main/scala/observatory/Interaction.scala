package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Visualization.{imageHeight, imageWidth, interpolateColor, predictTemperature}

import scala.math.{atan, sinh, toDegrees}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction extends InteractionInterface {

  private val tilePixelScale = 8
  private val imageSize = 256

  private lazy val allTilesForMap = generateRangeTiles(0, 3)

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = {
    Location(
      Math.round(toDegrees(atan(sinh(Math.PI * (1.0 - 2.0 * tile.y.toDouble / (1 << tile.zoom)))))),
      tile.x.toDouble / (1 << tile.zoom) * 360.0 - 180.0
    )
  }

  /**
    * @param temperatures Known temperatures
    * @param colors       Color scale
    * @param tile         Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    System.gc()
    val startTime = System.currentTimeMillis()
    println(s"Time: $startTime ms - tile generation start: $tile")
    val (leftTopTile, rightBottomTile) = zoomTile(tile, tilePixelScale)
    val zoomedTiles = for (y <- leftTopTile.y to rightBottomTile.y;
                           x <- leftTopTile.x to rightBottomTile.x)
    yield Tile(x, y, leftTopTile.zoom)

    def createPixel(targetLoc: Location): Pixel = {
      val predictedTemperature = predictTemperature(temperatures, targetLoc)
      val interpolatedColor = interpolateColor(colors, predictedTemperature)
      toPixel(interpolatedColor, 127)
    }

    val pixels = zoomedTiles.par.map(tileLocation).map(createPixel).toArray
    val image = Image(imageSize, imageSize, pixels)

    val timeTaken = System.currentTimeMillis() - startTime
    println(s"Time taken: $timeTaken ms - tile generation end: $tile")

    image
  }

  def zoomTile(tile: Tile, zoomLevel: Int): (Tile, Tile) = {
    def zoomTileRec(nextRange: (Tile, Tile), leftIterations: Int): (Tile, Tile) = {
      if (leftIterations <= 0) nextRange
      else {
        val left = nextRange._1
        val right = nextRange._2
        zoomTileRec((Tile(left.x * 2, left.y * 2, left.zoom + 1),
          Tile(right.x * 2 + 1, right.y * 2 + 1, right.zoom + 1)), leftIterations - 1)
      }
    }

    zoomTileRec((tile, tile), zoomLevel)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    *
    * @param yearlyData    Sequence of (year, data), where `data` is some data associated with
    *                      `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
                           yearlyData: Iterable[(Year, Data)],
                           generateImage: (Year, Tile, Data) => Unit
                         ): Unit = {
    System.gc()
    println("generateTiles start")
    val computedTiles = allTilesForMap
    yearlyData.par
      .flatMap(yearAndData => computedTiles.map(tile => (yearAndData._1, tile, yearAndData._2)))
      .foreach(data => generateImage(data._1, data._2, data._3))
  }

  def generateRangeTiles(fromZoom: Int, toZoom: Int): Iterable[Tile] = {
    (fromZoom to toZoom).flatMap(generateAllTiles)
  }

  def generateAllTiles(zoom: Int): Iterable[Tile] = {
    val side = 1 << zoom
    val total = side * side

    (0 until total).map(index => Tile(index / side, index % side, zoom))
  }

  def toPixel(color: Color, alpha: Int): Pixel = {
    color match {
      case Color(red, green, blue) => Pixel(red, green, blue, alpha)
    }
  }
}
