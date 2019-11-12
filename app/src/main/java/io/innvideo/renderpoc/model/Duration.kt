package io.innvideo.renderpoc.model

data class Duration(
    val absolute_duration: Int,
    val absolute_end: Int,
    val absolute_start: Int,
    val delay: String,
    val driver: Boolean,
    val element_duration: Double,
    val end_time: Int,
    val priority: Int,
    val split_others: Int,
    val split_self: Int,
    val start_time: Int
)