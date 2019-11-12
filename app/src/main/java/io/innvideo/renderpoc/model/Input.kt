package io.innvideo.renderpoc.model

data class Input(
    val allow_multiple: Int,
    val hidden: Int,
    val placeholder: String,
    val required: Int,
    val type: String
)