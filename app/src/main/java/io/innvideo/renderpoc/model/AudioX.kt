package io.innvideo.renderpoc.model

data class AudioX(
    val absolute_end: Int,
    val absolute_start: Int,
    val audio_volume: Double,
    val end_time: Int,
    val start_time: Int
)