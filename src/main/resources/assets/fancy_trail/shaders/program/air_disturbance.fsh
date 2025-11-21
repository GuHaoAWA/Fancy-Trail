#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D Mask;
uniform float DistortionStrength;
uniform float Time;
uniform vec2 Direction;

in vec2 texCoord;
out vec4 fragColor;

// 伪随机函数
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

vec2 random2(vec2 p) {
    return fract(
    sin(vec2(
    dot(p, vec2(127.1, 311.7)),
    dot(p, vec2(269.5, 183.3))
    )) * 43758.5453
    );
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    // 四个角点的随机值
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    // 双线性插值
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float perlinNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    // 四个角点的梯度
    vec2 grad00 = random2(i) * 2.0 - 1.0;
    vec2 grad10 = random2(i + vec2(1.0, 0.0)) * 2.0 - 1.0;
    vec2 grad01 = random2(i + vec2(0.0, 1.0)) * 2.0 - 1.0;
    vec2 grad11 = random2(i + vec2(1.0, 1.0)) * 2.0 - 1.0;

    // 距离向量
    vec2 dist00 = f;
    vec2 dist10 = f - vec2(1.0, 0.0);
    vec2 dist01 = f - vec2(0.0, 1.0);
    vec2 dist11 = f - vec2(1.0, 1.0);

    // 点乘
    float dot00 = dot(grad00, dist00);
    float dot10 = dot(grad10, dist10);
    float dot01 = dot(grad01, dist01);
    float dot11 = dot(grad11, dist11);

    // 平滑插值
    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(
    mix(dot00, dot10, u.x),
    mix(dot01, dot11, u.x),
    u.y
    ) * 0.5 + 0.5; // 映射到 [0,1]
}

float fbm(vec2 p, int octaves) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for(int i = 0; i < octaves; i++) {
        value += amplitude * perlinNoise(frequency * p);
        amplitude *= 0.5;
        frequency *= 2.0;
    }

    return value;
}

// 湍流噪声 - 用于更自然的流体效果
float turbulence(vec2 p, int octaves) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for(int i = 0; i < octaves; i++) {
        value += amplitude * abs(perlinNoise(frequency * p) - 0.5);
        amplitude *= 0.5;
        frequency *= 2.0;
    }

    return value;
}

void main() {
    vec4 mask = texture(Mask, texCoord);
    float alpha = mask.a;

    if (alpha > 0.001) {
        // 增强强度计算，考虑边缘衰减
        vec2 centerVec = texCoord - vec2(0.5);
        float centerDist = length(centerVec);
        float edgeFalloff = 1.0 - smoothstep(0.0, 0.3, centerDist);

        float strength = DistortionStrength * alpha * 0.15 * edgeFalloff;

        // 使用FBM噪声创建更自然的热浪扰动
        vec2 noiseCoord = texCoord * 4.0 + Time * 2.0;
        float fbmNoise = fbm(noiseCoord, 4) * 2.0 - 1.0;

        // 湍流噪声用于细节扰动
        float turb = turbulence(texCoord * 8.0 + Time * 3.0, 3) * 0.5;

        // 多层热浪扰动，现在使用更高质量的噪声
        float heatWave1 = sin(texCoord.x * 50.0 + texCoord.y * 30.0 + Time * 8.0 + fbmNoise) * strength;
        float heatWave2 = cos(texCoord.x * 40.0 - texCoord.y * 25.0 + Time * 6.0 + turb) * strength * 0.7;
        float heatWave3 = perlinNoise(texCoord * 15.0 + Time * 4.0) * strength * 0.8;

        // 高级噪声扰动
        float advancedNoiseDistort = fbm(texCoord * 12.0 + Time * 2.5, 3) * strength * 0.4;

        // 运动扭曲 - 使用噪声增强方向感
        vec2 noiseMotion = vec2(
        perlinNoise(texCoord * 6.0 + Time * 3.0) - 0.5,
        perlinNoise(texCoord * 6.0 + Time * 3.0 + 100.0) - 0.5
        ) * strength * 0.2;

        vec2 motionDistortion = Direction * strength * 0.15 * (0.8 + 0.4 * sin(Time * 5.0)) + noiseMotion;

        // 涡流效果 - 使用Perlin噪声创建更自然的漩涡
        vec2 vortexDir = vec2(-Direction.y, Direction.x);
        float vortexNoise = perlinNoise(texCoord * 8.0 + Time * 6.0) * 2.0 - 1.0;
        float vortex = (sin(texCoord.x * 35.0 + Time * 10.0) + vortexNoise * 0.3) * strength * 0.15;
        vec2 vortexDistortion = vortexDir * vortex;

        // 径向扭曲 - 使用FBM噪声增强冲击波效果
        float radialNoise = fbm(centerVec * 15.0 - Time * 8.0, 2);
        float radialWave = (sin(length(centerVec) * 20.0 - Time * 12.0) + radialNoise * 0.4) * strength * 0.1;
        vec2 radialDistortion = normalize(centerVec) * radialWave;

        // 组合所有扭曲效果
        vec2 totalDistortion = vec2(heatWave1 + heatWave3, heatWave2) +
        motionDistortion +
        vortexDistortion +
        radialDistortion +
        vec2(advancedNoiseDistort);

        vec2 distortedCoord = texCoord + totalDistortion;


        float chromaNoise = fbm(texCoord * 20.0 + Time, 2);
        // 将色差强度从 0.008 减少到 0.002
        float chromaStrength = strength * (0.001 + chromaNoise * 0.002);


        vec2 chromaRed = totalDistortion * (1.02 + chromaNoise * 0.01) + vec2(chromaStrength, chromaStrength * 0.3);
        vec2 chromaBlue = totalDistortion * (0.98 - chromaNoise * 0.01) - vec2(chromaStrength, chromaStrength * 0.2);

        float originalAlpha = texture(DiffuseSampler, texCoord).a;

        // 分别采样RGB通道 - 保持绿色通道较少失真
        vec4 baseColor = texture(DiffuseSampler, distortedCoord);
        float r = texture(DiffuseSampler, distortedCoord + chromaRed).r;
        float g = baseColor.g; // 直接使用基础颜色的绿色通道
        float b = texture(DiffuseSampler, distortedCoord + chromaBlue).b;

        fragColor = vec4(r, g, b, originalAlpha);

        // 动态亮度增强 - 基于噪声变化
        float brightnessNoise = fbm(texCoord * 8.0 + Time * 2.0, 2);
        float brightnessBoost = 1.0 + strength * (0.3 + brightnessNoise * 0.2);
        fragColor.rgb *= brightnessBoost;

        // 热浪颜色偏移 - 使用湍流噪声创建更自然的颜色变化
        float heatTurbulence = turbulence(texCoord * 10.0 + Time * 2.0, 2);
        float heatColorShift = strength * (0.01 + heatTurbulence * 0.02);

        // 偏蓝的热浪色调
        fragColor.r += heatColorShift * 0.1;
        fragColor.g += heatColorShift * 0.3;
        fragColor.b += heatColorShift * 0.6;

        // 动态边缘发光效果
        float glowIntensity = fbm(centerVec * 12.0 + Time * 3.0, 2);
        float glow = smoothstep(0.0, 0.2, centerDist) * strength * (0.5 + glowIntensity * 0.3);

        // 热浪发光颜色（蓝白色）
        vec3 glowColor = mix(vec3(0.3, 0.5, 0.8), vec3(0.8, 0.9, 1.0), glowIntensity);
        fragColor.rgb += glowColor * glow;

        // 添加微妙的对比度增强
        float contrast = 1.0 + strength * 0.2;
        fragColor.rgb = (fragColor.rgb - 0.5) * contrast + 0.5;

    } else {
        // 没有气流的区域保持原样
        fragColor = texture(DiffuseSampler, texCoord);
    }
}