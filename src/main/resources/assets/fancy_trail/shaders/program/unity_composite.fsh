#version 150

//copied https://github.com/Low-Drag-MC/Shimmer

uniform sampler2D DiffuseSampler;
uniform sampler2D DownTexture;
uniform sampler2D Background;
uniform vec2 OutSize;
uniform float BloomIntensive;
uniform float BloomBase;
uniform float BloomThresholdUp;
uniform float BloomThresholdDown;

in vec2 texCoord;
out vec4 fragColor;

const float WEIGHT_CENTER = 4.0;
const float WEIGHT_ADJACENT = 2.0;
const float WEIGHT_DIAGONAL = 1.0;
const float BLOOM_MULTIPLIER = 0.8;

float calculateLuminance(vec3 color) {
    return dot(color, vec3(0.299, 0.587, 0.114));
}

void main() {
    vec2 texelSize = 1.0 / OutSize;

    vec4 out_colour = texture(DiffuseSampler, texCoord) * WEIGHT_CENTER;
    out_colour += texture(DiffuseSampler, texCoord + vec2(texelSize.x, 0.0)) * WEIGHT_ADJACENT;
    out_colour += texture(DiffuseSampler, texCoord + vec2(-texelSize.x, 0.0)) * WEIGHT_ADJACENT;
    out_colour += texture(DiffuseSampler, texCoord + vec2(0.0, texelSize.y)) * WEIGHT_ADJACENT;
    out_colour += texture(DiffuseSampler, texCoord + vec2(0.0, -texelSize.y)) * WEIGHT_ADJACENT;
    out_colour += texture(DiffuseSampler, texCoord + vec2(texelSize.x, texelSize.y)) * WEIGHT_DIAGONAL;
    out_colour += texture(DiffuseSampler, texCoord + vec2(-texelSize.x, texelSize.y)) * WEIGHT_DIAGONAL;
    out_colour += texture(DiffuseSampler, texCoord + vec2(texelSize.x, -texelSize.y)) * WEIGHT_DIAGONAL;
    out_colour += texture(DiffuseSampler, texCoord + vec2(-texelSize.x, -texelSize.y)) * WEIGHT_DIAGONAL;

    out_colour /= (WEIGHT_CENTER + 4.0 * WEIGHT_ADJACENT + 4.0 * WEIGHT_DIAGONAL);

    vec4 highLight = texture(DownTexture, texCoord);
    vec4 bloom = BloomIntensive * vec4(out_colour.rgb * BLOOM_MULTIPLIER + highLight.rgb * BLOOM_MULTIPLIER, 1.0);

    vec4 background = texture(Background, texCoord);
    background.rgb = mix(background.rgb, highLight.rgb, highLight.a);

    float luminance = calculateLuminance(background.rgb);
    float bloomFactor = (1.0 - luminance) * (BloomThresholdUp - BloomThresholdDown) + BloomThresholdDown + BloomBase;

    fragColor = vec4(background.rgb + bloom.rgb * bloomFactor, 1.0);
}