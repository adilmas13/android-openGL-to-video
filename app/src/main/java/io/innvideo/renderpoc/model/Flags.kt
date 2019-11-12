package io.innvideo.renderpoc.model

data class Flags(
    val added: Boolean,
    val after: Any,
    val generateBlock: Boolean,
    val nounsChanged: Boolean,
    val recalculateDuration: Boolean,
    val split: Boolean,
    val textChanged: Boolean,
    val typeChanged: Boolean
)