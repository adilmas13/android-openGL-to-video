package io.innvideo.renderpoc.model

data class VideoDataModel(
    val account_id: Int,
    val audio_duration: Double,
    val audio_params: AudioParams,
    val audio_url: String,
    val blocks: List<Block> = mutableListOf(),
    val default_transition: Int,
    val dimensions: List<String>,
    val duration: DurationX,
    val editor_preview: String,
    val emotion: String,
    val headline: String,
    val master_video_id: Int,
    val no_watermark: Boolean,
    val premium: List<Any>,
    val screen_click: Int,
    val screen_edit: Int,
    val screen_size: List<Int>,
    val spans: List<Span>,
    val step: Int,
    val template_id: Int,
    val template_name: String,
    val template_type: String,
    val text: String,
    val transitions: List<Any>,
    val video_properties: VideoProperties
)