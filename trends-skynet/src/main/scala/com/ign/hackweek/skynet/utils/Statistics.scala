package com.ign.hackweek.skynet.utils

trait Statistics {

  def Mean(samples: List[Double]): Double = {
    if (samples.size > 0) {
      var sum = 0.0
      samples.foreach(x => sum += x )
      return sum / samples.size
    } else {
      0.0
    }
  }

  def StdDev(samples: List[Double]): Double = {
    val mean = Mean(samples)
    var sum = 0.0
    samples.foreach(x => sum += scala.math.pow(x-mean,2.0) )
    scala.math.sqrt(sum / samples.size)
  }

}
