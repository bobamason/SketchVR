#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision highp float;
#else
#define MED
#define LOWP
#define HIGH
#endif

uniform sampler2D u_texture;
varying vec2 v_texCoord0;

void main(){
    gl_FragColor = vec4(0.2, 1.0, 0.1, 1.0) * texture2D(u_texture, v_texCoord0);
}