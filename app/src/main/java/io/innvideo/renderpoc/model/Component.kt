package io.innvideo.renderpoc.model


data class Component(
    var name: String,
    var type: String,
    var components: MutableList<Component> = mutableListOf(),
    var layout: Layout,
    var duration: Duration,
    var animation: Animation,
    var audio: Audio,
    var position: Position,
    var input: Input,
    var id: String,
    var url: String,
    var metadata: Metadata,
    var sub_type: String,
    var orientation: Any,
    var word: Any,
    var entity_type: Any,
    var screen_edit: Int,
    var screen_click: String?,
    var sticker_id: String?,
    var media_properties: MediaProperties,
    var thumbnail_url: String,
    var media_type: String,
    var layer_name: String = "",
    var text: String = ""
)