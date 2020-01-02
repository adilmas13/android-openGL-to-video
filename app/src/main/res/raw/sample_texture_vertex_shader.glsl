// input attributes
attribute vec4 position;
attribute vec2 texCoord;
attribute vec4 inputColor;
// output attributes
varying vec4 interpolated_colour;
varying vec2 texture_coord;

void main()
{
    interpolated_colour = inputColor;
    texture_coord = texCoord;
    gl_Position = position;
}