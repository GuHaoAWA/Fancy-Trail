#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D Mask;
uniform float ChromaticStrength;

in vec2 texCoord;
out vec4 fragColor;

void main(){
    vec4 originalColor = texture(DiffuseSampler, texCoord);
    vec4 maskColor = texture(Mask, texCoord);

    if (maskColor.a > 0.001) {
        // 只在粒子区域应用色差叠加效果
        float strength = ChromaticStrength * maskColor.a;
        vec2 redOffset = vec2(strength * 0.005, 0.0);
        vec2 blueOffset = vec2(-strength * 0.005, 0.0);

        // 对原始画面应用色差（不是对粒子颜色）
        float r = texture(DiffuseSampler, texCoord + redOffset).r;
        float g = originalColor.g;
        float b = texture(DiffuseSampler, texCoord + blueOffset).b;

        // 使用粒子alpha作为混合权重
        float blendWeight = maskColor.a;
        vec3 chromaticColor = vec3(r, g, b);

        // 叠加混合：色差效果 + 原始画面
        vec3 finalColor = mix(originalColor.rgb, chromaticColor, blendWeight);

        fragColor = vec4(finalColor, originalColor.a);
    } else {
        // 非粒子区域保持原样
        fragColor = originalColor;
    }
}