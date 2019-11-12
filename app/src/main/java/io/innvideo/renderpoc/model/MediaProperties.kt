package io.innvideo.renderpoc.model


data class MediaProperties(
    var width: Int,
    var height: Int,
    var url: String,
    var word: String?,
    var image_link: String,
    var thumbnail_url: String,
    var source: String?,
    var search_license: String?,
    var partner_image_id: String?
)