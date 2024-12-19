package com.android.voyageur

import kotlin.math.sqrt

/**
 * A data class representing a point in a 2D Cartesian coordinate system.
 *
 * This class stores the coordinates (x, y) of the point and provides functionality
 * to calculate the Euclidean distance between the current point and another point.
 *
 * @property x The x-coordinate of the point.
 * @property y The y-coordinate of the point.
 */
data class Point(val x: Double, val y: Double) {

  fun distanceTo(p: Point): Double {
    val dx = x - p.x
    val dy = y - p.y
    return sqrt(dx * dx + dy * dy)
  }
}
