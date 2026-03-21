#version 150

uniform sampler2D DiffuseSampler;      // 主场景（背景）
uniform sampler2D Mask;                // 粒子纹理（刀光）

uniform float Time;
uniform float BlendStrength;
uniform float GlowIntensity;
uniform float AlphaBoost;

in vec2 texCoord;
out vec4 fragColor;

// 获取场景亮度
float getLuminance(vec3 color) {
    return dot(color, vec3(0.299, 0.587, 0.114));
}

// 边缘辉光
float edgeGlow(vec2 uv, float alpha) {
    vec2 texelSize = 1.0 / vec2(textureSize(Mask, 0));

    float alphaX = texture(Mask, uv + vec2(texelSize.x, 0.0)).a;
    float alphaY = texture(Mask, uv + vec2(0.0, texelSize.y)).a;
    float alphaNX = texture(Mask, uv - vec2(texelSize.x, 0.0)).a;
    float alphaNY = texture(Mask, uv - vec2(0.0, texelSize.y)).a;

    float edge = abs(alphaX - alphaNX) + abs(alphaY - alphaNY);
    return clamp(edge * 2.0 * GlowIntensity, 0.0, 0.8);
}

void main() {
    // 获取主场景和粒子
    vec3 sceneColor = texture(DiffuseSampler, texCoord).rgb;
    vec4 particle = texture(Mask, texCoord);

    float alpha = particle.a * AlphaBoost;

    if (alpha < 0.01) {
        // 没有刀光的区域：直接输出场景
        fragColor = vec4(sceneColor, 1.0);
        return;
    }

    // 有刀光的区域：混合
    float sceneLum = getLuminance(sceneColor);

    // 刀光基础颜色
    vec3 trailColor = particle.rgb;

    // 边缘辉光
    float glow = edgeGlow(texCoord, alpha);
    vec3 glowColor = vec3(0.9, 0.85, 0.8) * glow;

    // 自适应混合因子
    float lumFactor = 1.0 - clamp(sceneLum * 0.7, 0.0, 0.6);
    float blendAlpha = clamp(alpha * (0.5 + lumFactor * 0.5) * BlendStrength, 0.0, 1.0);

    // 最终颜色 = 刀光 + 辉光
    vec3 finalColor = trailColor + glowColor;
    finalColor = finalColor * (0.8 + sceneLum * 0.4);

    // 混合刀光和场景
    vec3 blended = mix(sceneColor, finalColor, blendAlpha);

    fragColor = vec4(blended, 1.0);
}