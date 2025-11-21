#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D Mask;
uniform sampler2D Content;

in vec2 texCoord;
out vec4 fragColor;

void main(){
    // 一次性采样所有纹理
    vec4 mask = texture(Mask, texCoord);
    vec3 org = texture(DiffuseSampler, texCoord).rgb;
    vec3 cont = texture(Content, texCoord).rgb;

    // 计算混合因子，避免分支
    float blendFactor = clamp(mask.a * 1000.0, 0.0, 1.0); // maskAlpha > 0.001 ? ~1.0 : 0.0

    // 使用mix进行混合
    vec3 finalColor = mix(org, cont * mask.rgb, mask.a * blendFactor);

    fragColor = vec4(finalColor, 1.0);
}