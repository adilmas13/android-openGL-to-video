package io.innvideo.renderpoc.editor.new_models.response_models

import com.google.gson.annotations.SerializedName

data class ParentResponseModel(
    @SerializedName("dimensions") val dimensions: List<String>,
    @SerializedName("blocks") val blocks: List<BlockResponseModel>
)

data class BlockResponseModel(
    @SerializedName("type") val type: String,
    @SerializedName("position") val position: PositionResponseModel,
    @SerializedName("components") val components: List<ComponentResponseModel>,
    @SerializedName("url") val url: String?
)

data class PositionResponseModel(
    @SerializedName("top_x") val topX: Float,
    @SerializedName("bottom_x") val bottomX: Float,
    @SerializedName("top_y") val topY: Float,
    @SerializedName("bottom_y") val bottomY: Float,
    @SerializedName("pivot_x") val pivotX: Float,
    @SerializedName("pivot_y") val pivotY: Float,
    @SerializedName("orientation") val orientation: String
)

data class ComponentResponseModel(
    @SerializedName("type") val type: String,
    @SerializedName("position") val position: PositionResponseModel,
    @SerializedName("components") val components: List<ComponentResponseModel>,
    @SerializedName("text") val text: String?,
    @SerializedName("url") val url: String?
)