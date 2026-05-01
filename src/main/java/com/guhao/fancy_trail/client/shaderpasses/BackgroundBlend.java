package com.guhao.fancy_trail.client.shaderpasses;

import com.guhao.vix.client.shaderpasses.PostPassBase;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

import static com.guhao.vix.client.pipeline.PostEffectPipelines.shaderOrthoMatrix;

public class BackgroundBlend extends PostPassBase {

    public BackgroundBlend(String resourceLocation, ResourceManager resmgr) throws IOException {
        super(resourceLocation, resmgr);
    }

    public void process(RenderTarget sceneTarget,     // 主场景（背景）
                        RenderTarget particleTarget,  // 粒子纹理（刀光）
                        RenderTarget outTarget,       // 输出目标
                        float blendStrength,
                        float glowIntensity,
                        float alphaBoost,
                        float time) {
        prevProcess(sceneTarget, outTarget);
        sceneTarget.unbindWrite();

        RenderSystem.viewport(0, 0, outTarget.width, outTarget.height);

        // 设置采样器 - 模仿 AirDisturbance 的模式
        this.effect.setSampler("DiffuseSampler", sceneTarget::getColorTextureId);      // 主场景
        this.effect.setSampler("Mask", particleTarget::getColorTextureId);             // 粒子纹理

        // 设置统一变量
        this.effect.safeGetUniform("ProjMat").set(shaderOrthoMatrix);
        this.effect.safeGetUniform("OutSize").set((float) outTarget.width, (float) outTarget.height);
        this.effect.safeGetUniform("BlendStrength").set(blendStrength);
        this.effect.safeGetUniform("GlowIntensity").set(glowIntensity);
        this.effect.safeGetUniform("AlphaBoost").set(alphaBoost);
        this.effect.safeGetUniform("Time").set(time);

        this.effect.apply();
        pushVertex(sceneTarget, outTarget);
        this.effect.clear();
        outTarget.unbindWrite();
        sceneTarget.unbindRead();
    }
}