// input attributes
uniform mat4 uMVPMatrix;
attribute vec4 position;
attribute vec4 inputColor;
// output attributes
varying vec4 interpolated_colour;

void main()
{
    interpolated_colour = inputColor;
    gl_Position = uMVPMatrix * position;

}