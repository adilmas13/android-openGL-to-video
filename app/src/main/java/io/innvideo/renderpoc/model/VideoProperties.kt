package io.innvideo.renderpoc.model

data class VideoProperties(
    val applied_colors: String,
    val brand_properties: BrandProperties,
    val colors: ColorsX,
    val colors_ref: ColorsRefX,
    val custom_properties: CustomProperties,
    val fonts: FontsXX,
    val template_properties: TemplateProperties
)