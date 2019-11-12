package io.innvideo.renderpoc.model

data class Audio(
    val audio_list: List<AudioX>,
    val audio_priority: Int,
    val audio_volume: Double,
    val bg_volume: Double,
    val end_fade_out_time: Int,
    val loop: Int,
    val playback_rate: Int,
    val standalone: Int,
    val url: String
)