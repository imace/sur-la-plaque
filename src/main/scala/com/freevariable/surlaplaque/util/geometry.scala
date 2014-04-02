/*
 * This file is a part of the "sur la plaque" toolkit for cycling
 * data analytics and visualization.
 *
 * Copyright (c) 2013--2014 William C. Benton and Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freevariable.surlaplaque.util

import com.freevariable.surlaplaque.data.{Coordinates, Trackpoint}

trait GeometryPrimitives {
  // TODO:  add some more basic primitives here to enable pushing more of
  // convex hull creation out to RDD-land
  
  def csub(a:Coordinates,b:Coordinates) = 
    Coordinates(a.lat - b.lat, a.lon - b.lon)
  
  def ccross(a:Coordinates,b:Coordinates) = 
    a.lat * b.lon - b.lat * a.lon
  
  def clockwise(o:Coordinates,a:Coordinates,b:Coordinates) =
    ccross(csub(a,o), csub(b,o)) <= 0
}

object ConvexHull extends GeometryPrimitives {
  def calculate(points: List[Coordinates]): List[Coordinates] = {
    val sortedPoints = points.sorted.distinct
    
    if (sortedPoints.length <= 2) {
      sortedPoints
    } else {
      val lowerHull = buildHull(sortedPoints, List())
      val upperHull = buildHull(sortedPoints.reverse, List())
      
      (lowerHull ++ upperHull).distinct
    }
  }
    
  private[this] def buildHull(points: List[Coordinates], hull: List[Coordinates]): List[Coordinates] = {
    (points, hull) match {
      case (Nil, _::tl) => tl.reverse
      case (p::ps, first::second::rest) => {
        if(clockwise(second, first, p)) {
          buildHull(points, second::rest)
        } else {
          buildHull(ps, p::hull)
        }
      }
      case (p::ps, ls) if ls.length < 2 => buildHull(ps, p::ls)
    }
  }
}