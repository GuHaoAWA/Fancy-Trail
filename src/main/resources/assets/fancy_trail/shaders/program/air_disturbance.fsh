#version 150

// 完全保留原有uniform变量，无任何新增/修改/删除
uniform sampler2D DiffuseSampler;
uniform sampler2D Mask;
uniform float DistortionStrength;
uniform float Time;
uniform vec2 Direction;

in vec2 texCoord;
out vec4 fragColor;

// ========================================
// 3A级噪声系统（优化梯度和采样逻辑）
// ========================================

// 高质量伪随机函数（避免周期性，3A游戏标准）
float hash(vec2 p) {
    p = fract(p * vec2(5.3983, 5.4427));
    p += dot(p, p + vec2(21.5351, 14.3137));
    return fract(p.x * p.y * 95.4337);
}

// 优化的梯度计算（各向异性，模拟真实流体）
vec2 grad(vec2 p) {
    float angle = hash(p) * 6.283185307;
    float mag = hash(p + vec2(0.1)) * 0.5 + 0.5;
    return vec2(cos(angle), sin(angle)) * mag;
}

// 3A级Perlin噪声（优化插值和梯度点乘，精简冗余计算）
float perlinNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0); // 5阶平滑插值

    vec2 g00 = grad(i);
    vec2 g10 = grad(i + vec2(1.0, 0.0));
    vec2 g01 = grad(i + vec2(0.0, 1.0));
    vec2 g11 = grad(i + vec2(1.0, 1.0));

    float dot00 = dot(g00, f);
    float dot10 = dot(g10, f - vec2(1.0, 0.0));
    float dot01 = dot(g01, f - vec2(0.0, 1.0));
    float dot11 = dot(g11, f - vec2(1.0, 1.0));

    float x1 = mix(dot00, dot10, u.x);
    float x2 = mix(dot01, dot11, u.x);
    return mix(x1, x2, u.y) * 0.6 + 0.5;
}

// 3A级FBM噪声（分层控制，适配不同距离，优化性能）
float fbm(vec2 p, int octaves, float lacunarity, float gain) {
    float value = 0.0;
    float amplitude = 1.0;
    float frequency = 1.0;
    float totalAmplitude = 0.0;

    int maxOctaves = min(octaves, 5);
    for(int i = 0; i < maxOctaves; i++) {
        value += amplitude * (perlinNoise(p * frequency) - 0.5);
        totalAmplitude += amplitude;
        amplitude *= gain;
        frequency *= lacunarity;
    }
    return totalAmplitude > 0.0 ? value / totalAmplitude * 2.0 : 0.0;
}

// 湍流噪声（模拟热浪的不规则扰动，精简实现）
float turbulence(vec2 p) {
    return abs(fbm(p, 4, 2.0, 0.5));
}

// ========================================
// 3A级扭曲系统（物理化+分层控制，精简冗余逻辑）
// ========================================

// 运动轨迹贴合的扭曲计算
vec2 getMotionDistortion(vec2 uv, float strength, float alpha) {
    vec2 dir = normalize(Direction);
    float noiseBase = Time * 3.0;
    vec2 noiseDir = vec2(
    perlinNoise(uv * 6.0 + noiseBase) - 0.5,
    perlinNoise(uv * 6.0 + noiseBase + 100.0) - 0.5
    ) * 0.2;

    float speedFalloff = 0.8 + 0.4 * sin(Time * 5.0 + uv.x * 3.0);
    return (dir * strength * 0.15 * speedFalloff + noiseDir) * alpha;
}

// 涡流扭曲（物理级漩涡，精简变量）
vec2 getVortexDistortion(vec2 uv, vec2 centerVec, float strength, float alpha) {
    vec2 vortexDir = normalize(vec2(-Direction.y, Direction.x));
    float vortexNoise = perlinNoise(uv * 8.0 + Time * 6.0) * 2.0 - 1.0;

    float vortexFalloff = 1.0 - smoothstep(0.0, 0.5, length(centerVec));
    float vortex = (sin(uv.x * 35.0 + Time * 10.0 + vortexNoise * 0.3) * strength * 0.15) * vortexFalloff;
    return vortexDir * vortex * alpha;
}

// 径向冲击波扭曲（模拟能量扩散，避免除0）
vec2 getRadialDistortion(vec2 centerVec, float strength, float alpha) {
    float radialNoise = fbm(centerVec * 15.0 - Time * 8.0, 2, 2.0, 0.5);
    float radialWave = (sin(length(centerVec) * 20.0 - Time * 12.0) + radialNoise * 0.4) * strength * 0.1;
    vec2 normCenter = normalize(centerVec + vec2(0.0001));
    return normCenter * radialWave * alpha;
}

// ========================================
// 主着色器逻辑（仅保留扭曲效果，无任何颜色改动）
// ========================================
void main() {
    vec4 mask = texture(Mask, texCoord);
    float alpha = mask.a;

    if (alpha > 0.001) {
        vec2 centerVec = texCoord - vec2(0.5);
        float centerDist = length(centerVec);
        float edgeFalloff = 1.0 - smoothstep(0.0, 0.3, centerDist);
        float strength = DistortionStrength * alpha * 0.15 * edgeFalloff;

        vec2 uv = texCoord;
        vec2 noiseCoordBase = uv * 4.0 + Time * 2.0;
        float fbmNoise = fbm(noiseCoordBase, 4, 2.0, 0.5);

        float heatWave1 = sin(uv.x * 50.0 + uv.y * 30.0 + Time * 8.0 + fbmNoise) * strength;
        float heatWave2 = cos(uv.x * 40.0 - uv.y * 25.0 + Time * 6.0 + turbulence(uv * 8.0 + Time * 3.0) * 0.5) * strength * 0.7;
        float heatWave3 = perlinNoise(uv * 15.0 + Time * 4.0) * strength * 0.8;

        vec2 baseDistortion = vec2(heatWave1 + heatWave3, heatWave2);
        vec2 motionDistortion = getMotionDistortion(uv, strength, alpha);
        vec2 vortexDistortion = getVortexDistortion(uv, centerVec, strength, alpha);
        vec2 radialDistortion = getRadialDistortion(centerVec, strength, alpha);
        vec2 advancedNoiseDistort = vec2(fbm(uv * 12.0 + Time * 2.5, 3, 2.0, 0.5)) * strength * 0.4;

        vec2 totalDistortion =
        baseDistortion * 0.8 +
        motionDistortion * 1.0 +
        vortexDistortion * 0.9 +
        radialDistortion * 0.7 +
        advancedNoiseDistort * 0.5;

        vec2 distortedCoord = uv + totalDistortion;
        vec4 originalScene = texture(DiffuseSampler, uv);
        vec4 distortedScene = texture(DiffuseSampler, distortedCoord);

        // 纯扭曲输出：不改变任何颜色属性
        fragColor = vec4(distortedScene.rgb, originalScene.a);
    } else {
        fragColor = texture(DiffuseSampler, texCoord);
    }
}