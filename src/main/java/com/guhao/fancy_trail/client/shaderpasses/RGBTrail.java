package com.guhao.fancy_trail.client.shaderpasses;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

import static com.guhao.fancy_trail.client.pipeline.PostEffectPipelines.shaderOrthoMatrix;

public class RGBTrail extends PostPassBase {
    public RGBTrail(String resourceLocation, ResourceManager resmgr) throws IOException {
        super(resourceLocation, resmgr);
    }

    public void process(RenderTarget inTarget, RenderTarget mask, RenderTarget outTarget,
                        float time, float intensity, float starScale,
                        float rainbowSpeed, float rainbowScale, float rainbowMix, float opacity) {
        prevProcess(inTarget, outTarget);
        inTarget.unbindWrite();

        RenderSystem.viewport(0, 0, outTarget.width, outTarget.height);
        this.effect.setSampler("DiffuseSampler", inTarget::getColorTextureId);
        this.effect.setSampler("Mask", mask::getColorTextureId);

        this.effect.safeGetUniform("ProjMat").set(shaderOrthoMatrix);
        this.effect.safeGetUniform("OutSize").set((float) outTarget.width, (float) outTarget.height);
        this.effect.safeGetUniform("Time").set(time);
        this.effect.safeGetUniform("Intensity").set(intensity);
        this.effect.safeGetUniform("StarScale").set(starScale);
        this.effect.safeGetUniform("RainbowSpeed").set(rainbowSpeed);
        this.effect.safeGetUniform("RainbowScale").set(rainbowScale);
        this.effect.safeGetUniform("RainbowMix").set(rainbowMix);
        this.effect.safeGetUniform("Opacity").set(opacity);

        this.effect.apply();
        pushVertex(inTarget, outTarget);
        this.effect.clear();
        outTarget.unbindWrite();
        inTarget.unbindRead();
    }

    // 简化版本
    public void process(RenderTarget inTarget, RenderTarget mask, RenderTarget outTarget, float time) {
        process(inTarget, mask, outTarget, time, 1.0f, 3.0f, 0.08f, 1.2f, 0.8f, 0.7f);
    }
}