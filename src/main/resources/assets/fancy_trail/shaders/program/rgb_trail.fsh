#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D Mask;
uniform float Time;
uniform float Intensity;
uniform float StarScale;
uniform float RainbowSpeed;
uniform float RainbowScale;
uniform float RainbowMix;
uniform float Opacity;

in vec2 texCoord;
out vec4 fragColor;

// 旋转矩阵
mat2 rot(float a) {
    float c = cos(a), s = sin(a);
    return mat2(c, -s, s, c);
}

// 彩虹颜色
vec3 rainbow(float h) {
    float x = fract(h);
    const float TAU = 6.2831853;
    return 0.5 + 0.5 * cos(TAU * (x + vec3(0.0, 0.3333333, 0.6666667)));
}

// 星空颜色数组
const vec3 COLORS[16] = vec3[16](
vec3(0.045, 0.180, 0.220),  // 深蓝
vec3(0.025, 0.160, 0.150),  // 深青
vec3(0.055, 0.190, 0.180),  // 青蓝
vec3(0.075, 0.200, 0.190),  // 亮青
vec3(0.095, 0.210, 0.160),  // 蓝绿
vec3(0.085, 0.140, 0.220),  // 紫蓝
vec3(0.120, 0.180, 0.280),  // 天蓝
vec3(0.140, 0.250, 0.140),  // 绿色
vec3(0.160, 0.220, 0.320),  // 淡紫
vec3(0.150, 0.180, 0.300),  // 紫蓝
vec3(0.200, 0.230, 0.240),  // 灰蓝
vec3(0.110, 0.380, 0.360),  // 青绿
vec3(0.280, 0.220, 0.320),  // 紫红
vec3(0.080, 0.480, 0.490),  // 亮青
vec3(0.320, 0.580, 0.460),  // 绿青
vec3(0.140, 0.480, 0.880)   // 亮蓝
);

// 星空层变换
mat4 star_layer(float layer) {
    float t = Time * 0.05;
    float layerTimeOffset = layer * 0.8;

    // 移动和旋转
    float tx = sin(t + layerTimeOffset) * 1.5 + layer * 0.4;
    float ty = cos(t + layerTimeOffset) * 1.5 + layer * 0.3;

    float rotationSpeed = 0.15 + layer * 0.08;
    float angle = t * rotationSpeed + layer * 60.0;
    mat2 R = rot(radians(angle));

    // 缩放
    float baseScale = 2.0 + sin(layer * 0.7) * 0.5;
    float sc = baseScale * StarScale;

    // 变换矩阵
    mat4 scaleRotate = mat4(
    sc * R[0][0], sc * R[0][1], 0.0, 0.0,
    sc * R[1][0], sc * R[1][1], 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
    );

    mat4 translate = mat4(
    1.0, 0.0, 0.0, tx,
    0.0, 1.0, 0.0, ty,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
    );

    // 缩放和平移矩阵
    mat4 scaleTranslate = mat4(
    0.5, 0.0, 0.0, 0.25,
    0.0, 0.5, 0.0, 0.25,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
    );

    return scaleRotate * translate * scaleTranslate;
}

void main() {
    vec4 originalColor = texture(DiffuseSampler, texCoord);
    vec4 maskColor = texture(Mask, texCoord);

    if (maskColor.a > 0.001) {
        float maskAlpha = maskColor.a * Intensity;

        // 基础彩虹效果
        float baseHue = Time * RainbowSpeed +
        dot(texCoord, vec2(1.0)) * RainbowScale;
        vec3 baseRainbow = rainbow(baseHue);

        // 基础颜色使用星空色
        vec3 baseTint = mix(COLORS[0], baseRainbow, RainbowMix);
        vec3 color = baseTint;

        // 多层星空效果
        int layers = 8; // 减少层数以适应刀光
        for(int i = 0; i < layers; i++) {
            vec4 layerCoord = vec4(texCoord, 0.0, 1.0) * star_layer(float(i + 1));

            if (layerCoord.w > 0.0) {
                vec2 layerUV = layerCoord.xy / layerCoord.w;

                // 采样原始纹理作为星点
                vec3 sampleColor = texture(DiffuseSampler, layerUV).rgb;

                // 每层不同的彩虹色调
                float layerHue = baseHue + float(i) * 0.12 +
                dot(layerUV, vec2(0.7)) * RainbowScale * 0.3;
                vec3 layerRainbow = rainbow(layerHue);
                vec3 layerTint = mix(COLORS[i % 16], layerRainbow, RainbowMix);

                // 层透明度衰减
                float layerOpacity = 1.0 - float(i) * 0.15;
                layerOpacity *= maskAlpha;

                color += sampleColor * layerTint * layerOpacity;
            }
        }

        // 限制颜色范围并应用透明度
        vec3 finalColor = min(color * 1.5, vec3(1.0));
        float finalAlpha = Opacity * maskAlpha;

        // 与原始画面混合
        vec3 blendedColor = mix(originalColor.rgb, finalColor, finalAlpha);
        fragColor = vec4(blendedColor, originalColor.a);

    } else {
        fragColor = originalColor;
    }
}