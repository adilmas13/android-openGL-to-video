package io.innvideo.renderpoc.model

data class Span(
    val api_modal_input: List<Any>,
    val epic_result: EpicResultX,
    val flags: Flags,
    val heading_text: List<Any>,
    val id: String,
    val lock_scene: Boolean,
    val meta: Meta,
    val no_text: List<Any>,
    val nouns: List<Any>,
    val posttext: List<Any>,
    val pretext: List<Any>,
    val sub_type: String,
    val text: List<Any>,
    val type: String
)