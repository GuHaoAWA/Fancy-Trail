#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D Mask;
uniform float InterScale;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 mask = texture(Mask, texCoord);
    float distortionStrength = InterScale * mask.a;

    // 增强扭曲强度的系数
    float intensityMultiplier = 1.054;

    if (distortionStrength > 0.001) {
        float oneOverScaled = 1.0 / (distortionStrength * intensityMultiplier);
        vec2 distortionOffset = (distortionStrength * intensityMultiplier - 1.0) * mask.gb;
        vec2 patchedCoord = texCoord * oneOverScaled + distortionOffset;

        fragColor = texture(DiffuseSampler, patchedCoord);
        fragColor.rgb = fragColor.rgb * (1.05 + mask.r * 0.1);
        fragColor.a = 1.0;
    } else {
        fragColor = texture(DiffuseSampler, texCoord);
    }
}