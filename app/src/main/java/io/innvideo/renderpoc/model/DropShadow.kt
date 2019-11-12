package io.innvideo.renderpoc.model

data class DropShadow(
    val angle: Int,
    val blur: Int,
    val colors_ref: ColorsRef,
    val distance: Int,
    val drop_shadow_color: List<Int>,
    val enabled: String,
    val name: String,
    val opacity: Int
)