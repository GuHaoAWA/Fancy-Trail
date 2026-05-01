#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D Mask;
uniform float Strength;
uniform float Time;
uniform float DecaySpeed;
uniform float MotionBlurFactor;
uniform float ColorShift;
uniform vec2 Direction;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 mask = texture(Mask, texCoord);
    float alpha = mask.a;

    if (alpha > 0.001) {
        // 当前帧颜色
        vec4 currentColor = texture(DiffuseSampler, texCoord);

        // 运动方向
        vec2 dir = normalize(Direction + vec2(0.001));

        // 多层残影
        vec4 accumulatedAfterImage = vec4(0.0);
        float totalWeight = 0.0;

        // 残影层数
        int layers = 5;
        float baseOffset = 0.02;

        for(int i = 1; i <= layers; i++) {
            float offsetAmount = float(i) * baseOffset * MotionBlurFactor;
            vec2 offsetUV = texCoord - dir * offsetAmount;

            // 限制在纹理范围内
            offsetUV = clamp(offsetUV, 0.001, 0.999);

            vec4 sampleColor = texture(DiffuseSampler, offsetUV);
            vec4 sampleMask = texture(Mask, offsetUV);

            // 残影透明度衰减，同时考虑遮罩
            float fade = exp(-offsetAmount * DecaySpeed) * sampleMask.a;

            // 只在遮罩区域内累加
            if(sampleMask.a > 0.001) {
                accumulatedAfterImage += sampleColor * fade;
                totalWeight += fade;
            }
        }

        // 如果有有效的残影采样
        if(totalWeight > 0.001) {
            // 归一化残影
            vec4 afterImage = accumulatedAfterImage / totalWeight;

            // 使用预乘alpha混合，避免白色残留
            vec3 blended = mix(currentColor.rgb, afterImage.rgb, Strength * alpha);

            fragColor = vec4(blended, currentColor.a);
        } else {
            fragColor = currentColor;
        }

    } else {
        // 没有刀光的区域保持原样
        fragColor = texture(DiffuseSampler, texCoord);
    }
}