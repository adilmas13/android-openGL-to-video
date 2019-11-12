package io.innvideo.renderpoc.model

data class Position(
    val orientation: String,
    var top_x: Double = 0.00,
    var bottom_x: Double = 0.00,
    var top_y: Double = 0.00,
    var bottom_y: Double = 0.00,
    var pivot_x: Double = 0.00,
    var pivot_y: Double = 0.00
)