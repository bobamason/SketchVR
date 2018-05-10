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

define PI 3.14159265

uniform sampler2D u_texture;
varying vec2 v_texCoord0;
varying LOWP vec4 v_color;

vec3 hsv2rgb(vec3 c) {
  vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
  vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
  return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 rgb2hsv(vec3 c) {
     vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
     vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
     vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
  
     float d = q.x - min(q.w, q.y);
     float e = 1.0e-10;
     return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
 }

void main(){
    vec2 center = vec2(0.5, 0.5);
    vec2 tc = (v_texCoord0 - center) * 2.0;
    float r = length(tc);
    if(r < 1.0)
        discard;
    float a = atan(tc.y, tc.x);
    vec3 hsv = vec3((a + PI) / (2.0 * PI), r, 1.0);
    gl_FragColor = v_color * vec4(hsv2rgb(hsv, 1.0));
}