#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D Mask;
uniform sampler2D StarTexture;
uniform float Time;
uniform float Intensity;
uniform float StarScale;
uniform float Opacity;
uniform int Layers;

in vec2 texCoord;
out vec4 fragColor;

mat2 rot(float a) {
    float c = cos(a), s = sin(a);
    return mat2(c, -s, s, c);
}

const mat4 SCALE_TRANSLATE = mat4(
0.5, 0.0, 0.0, 0.25,
0.0, 0.5, 0.0, 0.25,
0.0, 0.0, 1.0, 0.0,
0.0, 0.0, 0.0, 1.0
);

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

mat4 star_layer(float layer) {
    float t = Time * 0.01;
    float layerTimeOffset = layer * 0.4;

    float tx = sin(t + layerTimeOffset) * 0.8 + layer * 0.2;
    float ty = cos(t + layerTimeOffset) * 0.8 + layer * 0.2;

    float rotationSpeed = 0.05 + layer * 0.03;
    float angle = t * rotationSpeed + layer * 30.0;
    mat2 R = rot(radians(angle));

    float baseScale = 2.0 + sin(layer * 0.7) * 0.5;
    float sc = baseScale * StarScale;

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

    return scaleRotate * translate * SCALE_TRANSLATE;
}

void main() {
    vec4 originalColor = texture(DiffuseSampler, texCoord);
    vec4 maskColor = texture(Mask, texCoord);

    if (maskColor.a > 0.001) {
        float maskAlpha = maskColor.a * Intensity;

        vec3 totalGlow = vec3(0.0);

        int n = min(Layers, 1);
        for(int i = 0; i < n; i++) {
            float layer = float(i + 1);
            vec4 layerCoord = vec4(texCoord, 0.0, 1.0) * star_layer(layer);

            if (layerCoord.w > 0.0) {
                vec2 layerUV = layerCoord.xy / layerCoord.w;

                // 直接使用贴图的RGB颜色
                vec4 starSample = texture(StarTexture, layerUV);

                // 直接使用亮度值，不设阈值
                float starValue = dot(starSample.rgb, vec3(0.299, 0.587, 0.114));

                // 增强亮度 - 关键修改！
                float brightness = starValue * 5.0; // 大幅增加亮度

                if (brightness > 0.01) {
                    // 使用贴图本身的颜色，但增强饱和度
                    vec3 layerTint = starSample.rgb * 1.5;

                    float layerOpacity = (1.0 - float(i) * 0.12) * maskAlpha * brightness;

                    // 闪烁效果
                    float flicker = sin(Time * 2.0 + hash(layerUV) * 6.283) * 0.3 + 0.7;
                    layerOpacity *= flicker;

                    // 直接叠加发光，不平均化
                    totalGlow += layerTint * layerOpacity * Opacity * 5.0;
                }
            }
        }

        // 直接加法混合，让星星更亮
        vec3 finalColor = originalColor.rgb + totalGlow;

        // 限制最大亮度避免过曝
        finalColor = min(finalColor, vec3(2.0));

        fragColor = vec4(finalColor, originalColor.a);

    } else {
        fragColor = originalColor;
    }
}