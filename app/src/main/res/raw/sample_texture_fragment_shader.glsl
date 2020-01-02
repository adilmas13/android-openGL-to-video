precision mediump float;
// input
varying vec4 interpolated_colour;
varying vec2 texture_coord;
uniform sampler2D texture_sampler;
void main()
{
//    gl_FragColor = interpolated_colour;
    gl_FragColor = texture2D(texture_sampler, texture_coord) * interpolated_colour;
}