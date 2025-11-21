#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform vec2 InSize;

in vec2 texCoord;
out vec4 fragColor;

void main(){
    vec2 pixelSize = 1.0 / InSize;

    // 中心像素
    vec4 center = texture(DiffuseSampler, texCoord);

    // 只采样直接相邻的4个像素进行边缘检测
    vec4 left = texture(DiffuseSampler, texCoord + vec2(-pixelSize.x, 0));
    vec4 right = texture(DiffuseSampler, texCoord + vec2(pixelSize.x, 0));
    vec4 top = texture(DiffuseSampler, texCoord + vec2(0, pixelSize.y));
    vec4 bottom = texture(DiffuseSampler, texCoord + vec2(0, -pixelSize.y));

    // 简单的边缘检测
    float edge = length(center.rgb - left.rgb) +
    length(center.rgb - right.rgb) +
    length(center.rgb - top.rgb) +
    length(center.rgb - bottom.rgb);

    // 只在边缘区域进行混合
    if (edge > 0.1) {
        vec4 blended = (center + left + right + top + bottom) * 0.2;
        fragColor = mix(center, blended, min(edge * 2.0, 1.0));
    } else {
        fragColor = center;
    }
}