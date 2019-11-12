package io.innvideo.renderpoc.model

data class Block(
    val animation: Animation,
    val audio: Audio,
    val audio_params: AudioParamsX,
    val block_id: Int,
    val block_preference: Any,
    val block_type: String,
    val components: MutableList<Component> = mutableListOf(),
    val duration: Duration,
    val edits: Int,
    val epic_result: EpicResult,
    val id: String,
    val input: Input,
    val input_list: List<Any>,
    val keep_logo: Boolean,
    val layout: Layout,
    val name: String,
    val nouns: List<Any>,
    val position: Position,
    val screen_click: Int,
    val screen_edit: Int,
    val screen_size: List<Int>,
    val sr_no: Int,
    val sub_type: String,
    val type: String,
    val url: String
)