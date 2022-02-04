package observatory

/**
  * Introduced in Week 1. Represents a location on the globe.
  *
  * @param lat Degrees of latitude, -90 ≤ lat ≤ 90
  * @param lon Degrees of longitude, -180 ≤ lon ≤ 180
  */
case class Location(lat: Double, lon: Double) {

  private val MAX_LAT = 90
  private val MAX_LON = 180
  private val AVERAGE_RADIUS_OF_EARTH_KM = 6371

  def greatCircleDistance(other: Location): Double = {
    val latDistance = Math.toRadians(this.lat - other.lat)
    val lngDistance = Math.toRadians(this.lon - other.lon)
    val sinLat = Math.sin(latDistance / 2)
    val sinLng = Math.sin(lngDistance / 2)

    val a = sinLat * sinLat +
      (Math.cos(Math.toRadians(this.lat)) *
        Math.cos(Math.toRadians(other.lat)) *
        sinLng * sinLng)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    AVERAGE_RADIUS_OF_EARTH_KM * c
  }

  def getGridSquare(): (GridLocation, GridLocation) = {
    val ceiledLat = lat.ceil.toInt
    val lat0 = if (ceiledLat == -MAX_LAT) ceiledLat + 1 else ceiledLat
    val lat1 = if (lat0 == -MAX_LAT + 1) MAX_LAT else lat0 - 1

    val flooredLon = Math.floor(lon).toInt
    val lon0 = if(flooredLon == MAX_LON) flooredLon - 1 else flooredLon
    val lon1 = if(lon0 == MAX_LON - 1) -MAX_LON else lon0 + 1

    (
      GridLocation(lat0, lon0),
      GridLocation(lat1, lon1)
    )
  }

}
/**
  * Introduced in Week 3. Represents a tiled web map tile.
  * See https://en.wikipedia.org/wiki/Tiled_web_map
  * Based on http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
  * @param x X coordinate of the tile
  * @param y Y coordinate of the tile
  * @param zoom Zoom level, 0 ≤ zoom ≤ 19
  */
case class Tile(x: Int, y: Int, zoom: Int)

/**
  * Introduced in Week 4. Represents a point on a grid composed of
  * circles of latitudes and lines of longitude.
  * @param lat Circle of latitude in degrees, -89 ≤ lat ≤ 90
  * @param lon Line of longitude in degrees, -180 ≤ lon ≤ 179
  */
case class GridLocation(lat: Int, lon: Int) {

  def toLocation(): Location = {
    Location(lat, lon)
  }
}


/**
  * Introduced in Week 5. Represents a point inside of a grid cell.
  * @param x X coordinate inside the cell, 0 ≤ x ≤ 1
  * @param y Y coordinate inside the cell, 0 ≤ y ≤ 1
  */
case class CellPoint(x: Double, y: Double)

/**
  * Introduced in Week 2. Represents an RGB color.
  * @param red Level of red, 0 ≤ red ≤ 255
  * @param green Level of green, 0 ≤ green ≤ 255
  * @param blue Level of blue, 0 ≤ blue ≤ 255
  */
case class Color(red: Int, green: Int, blue: Int) {
}

case class StationKey(stn: String, wban: String)

case class Station(key: StationKey, loc: Location)

case class TemperatureRow(key: StationKey, month: Int, day: Int, temp: Temperature)
