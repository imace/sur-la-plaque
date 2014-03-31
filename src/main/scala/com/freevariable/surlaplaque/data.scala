package com.freevariable.surlaplaque.data;

import com.github.nscala_time.time.Imports._

sealed case class Coordinates(lat: Double, lon: Double) extends Ordered[Coordinates] {
  import com.freevariable.surlaplaque.util.RWDistance.{distance => rw_distance}
  
  /**
    Approximate distance between this and other in meters
  */
  def distance(other:Coordinates) = rw_distance((lat, lon), (other.lat, other.lon))

  /**
    Ordering based on longitude then latitude
  */
  def compare(other: Coordinates) = 
    implicitly[Ordering[(Double, Double)]].compare((this.lon, this.lat), (other.lon, other.lat))
  
  /** 
    Ordering based on latitude (then longitude, if necessary) 
  */
  def compare_lat(other: Coordinates) = 
    implicitly[Ordering[(Double, Double)]].compare((this.lat, this.lon), (other.lat, other.lon))
  
  /**
    Returns true if this is strictly "to the left of" (viz., to the west of, or to the south of if this and other are equally-westerly) other
    */
  def strictlyLeft(other: Coordinates) = (this compare other) < 0
  
  def strictlyRight(other: Coordinates) = (this compare other) > 0
  
  def strictlyAbove(other: Coordinates) = (this compare_lat other) > 0
  
  def strictlyBelow(other: Coordinates) = (this compare_lat other) < 0

}

sealed case class Trackpoint(timestamp: Long, latlong: Coordinates, altitude: Double, watts: Double, activity: Option[String]) {
    val timestring = Timestamp.stringify(timestamp)
    
    def elevDelta(other: Trackpoint) = other.altitude - altitude
    def timeDelta(other: Trackpoint) = (other.timestamp - timestamp).toDouble / 1000
    def distanceDelta(other: Trackpoint) = (other.latlong.distance(latlong))
    def kphBetween(other:Trackpoint) = ((other.latlong.distance(latlong)) / timeDelta(other)) * 3600
    def gradeBetween(other:Trackpoint) = {
        val rise = elevDelta(other) // rise is in meters
        val run = distanceDelta(other) * 10 // run is in km, but we want to get a percentage grade
        rise/run
    }
}

object Timestamp {
    def stringify(ts: Long) = ts.toDateTime.toString()
}

object Trackpoint {
    def apply(ts_string: String, latlong: Coordinates, altitude: Double, watts: Double, activity: Option[String] = None) = 
        new Trackpoint(ts_string.toDateTime.millis, latlong, altitude, watts, activity)
}
