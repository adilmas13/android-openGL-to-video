package io.innvideo.renderpoc

sealed class Layer() {
    class Video(val filePath: String) : Layer()
    class Image(val image: String) : Layer()
    class Text(val text: String, val textSize: Int, val textColor: String) : Layer()
    class Background(val red: Float, val green: Float, val blue: Float, val alpha: Float) : Layer()


}