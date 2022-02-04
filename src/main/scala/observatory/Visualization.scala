package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends VisualizationInterface {

  private val p = 6
  private val imageWidth = 360
  private val imageHeight = 180
  private val imageSizeInPixels = imageWidth * imageHeight

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location     Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    val (weightedSumMean, weightedSum) = temperatures.par.map(locWithTemp => {
      val distance = location.greatCircleDistance(locWithTemp._1)
      if(distance == 0.0) (locWithTemp._2, 0.0) else {
        val wix = 1 / Math.pow(distance, p)
        (locWithTemp._2 * wix, wix)
      }
    }).reduce((acc1, acc2) => {
      if(acc1._2 == 0.0) (acc1._1, 0.0)
      else if (acc2._2 == 0.0) (acc2._1, 0.0)
      else (acc1._1 + acc2._1, acc1._2 + acc2._2)
    })

    if (weightedSum == 0.0) weightedSumMean else weightedSumMean / weightedSum
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value  The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    def findRange(): ((Temperature, Color), (Temperature, Color)) = {
      val sortedPoints = points.toSeq.sortBy(_._1)
      sortedPoints.lastIndexWhere(_._1 < value) match {
        case -1 => (sortedPoints.head, sortedPoints.head)
        case x if x >= 0 && x < sortedPoints.size - 1 => (sortedPoints(x), sortedPoints(x + 1))
        case x => (sortedPoints(x), sortedPoints(x))
      }
    }

    val range = findRange()
    val ((lowTemp, lowColor), (highTemp, highColor)) = range
    Color(
      linearInterpolation((lowTemp, lowColor.red), (highTemp, highColor.red))(value),
      linearInterpolation((lowTemp, lowColor.green), (highTemp, highColor.green))(value),
      linearInterpolation((lowTemp, lowColor.blue), (highTemp, highColor.blue))(value)
    )
  }

  def linearInterpolation(xy0: (Double, Int), xy1: (Double, Int))(x: Double): Int = {
    val (x0, y0) = xy0
    val (x1, y1) = xy1

    val y = if (x0 != x1) (y0 * (x1 - x) + y1 * (x - x0)) / (x1 - x0) else y0
    Math.round(y).toInt
  }

  /**
    * @param temperatures Known temperatures
    * @param colors       Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    println("visualize start")

    def createPixel(pixelIndex: Int): Pixel = {
      val (arrayX, arrayY) = (pixelIndex / imageWidth, pixelIndex % imageWidth)
      val targetLoc = Location(imageHeight / 2 - arrayX, arrayY - imageWidth / 2)
      val predictedTemperature = predictTemperature(temperatures, targetLoc)
      val interpolatedColor = interpolateColor(colors, predictedTemperature)
      toPixel(interpolatedColor)
    }

    val pixels = (0 until imageSizeInPixels).par.map(createPixel).toArray
    Image(imageWidth, imageHeight, pixels)
  }

  private def toPixel(color: Color): Pixel = {
    color match {
      case Color(red, green, blue) => Pixel(red, green, blue, 255)
    }
  }

}

