#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D BlurTexture1;
uniform sampler2D BlurTexture2;
uniform sampler2D BlurTexture3;
uniform sampler2D BlurTexture4;
uniform float BloomRadius;
uniform float BloomIntensive;

in vec2 texCoord;
out vec4 fragColor;

// 预计算lerp因子，避免重复计算
vec4 precomputeBloomFactors() {
    float mirrorFactor1 = 1.2 - 1.0;
    float mirrorFactor2 = 1.2 - 0.8;
    float mirrorFactor3 = 1.2 - 0.6;
    float mirrorFactor4 = 1.2 - 0.4;

    float factor1 = mix(1.0, mirrorFactor1, BloomRadius);
    float factor2 = mix(0.8, mirrorFactor2, BloomRadius);
    float factor3 = mix(0.6, mirrorFactor3, BloomRadius);
    float factor4 = mix(0.4, mirrorFactor4, BloomRadius);

    return vec4(factor1, factor2, factor3, factor4) * BloomIntensive;
}

// 优化版的Jodie Reinhard色调映射 - 简化版本
vec3 fastTonemap(vec3 c) {
    // 使用近似公式替代复杂计算
    vec3 tc = c / (c + 1.0);
    float l = max(max(c.r, c.g), c.b) * 0.5; // 简化亮度计算
    return mix(c * (1.0 - l), tc, tc); // 简化混合
}

// 极简ACES近似 (可选)
vec3 fastACES(vec3 x) {
    return x * (2.5 * x + 0.1) / (x * (2.2 * x + 1.5) + 0.1);
}

void main() {
    // 预计算所有Bloom权重因子
    vec4 bloomFactors = precomputeBloomFactors();

    // 一次性采样所有Bloom纹理
    vec4 bloom1 = texture(BlurTexture1, texCoord);
    vec4 bloom2 = texture(BlurTexture2, texCoord);
    vec4 bloom3 = texture(BlurTexture3, texCoord);
    vec4 bloom4 = texture(BlurTexture4, texCoord);

    // 合并Bloom贡献
    vec3 bloomSum = bloom1.rgb * bloomFactors.x +
    bloom2.rgb * bloomFactors.y +
    bloom3.rgb * bloomFactors.z +
    bloom4.rgb * bloomFactors.w;

    // 采样主纹理一次
    vec4 background = texture(DiffuseSampler, texCoord);

    // 优化高光合成 - 使用预乘Alpha避免额外采样
    // 假设highLight是background的一部分，直接使用background的alpha
    vec3 finalBackground = background.rgb * (1.0 - background.a) + background.a * background.rgb;

    // 应用快速色调映射并合成
    vec3 tonemappedBloom = fastTonemap(bloomSum);
    fragColor = vec4(finalBackground + tonemappedBloom, 1.0);
}