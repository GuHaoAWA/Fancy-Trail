package com.guhao.fancy_trail.client.shaderpasses;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

import static com.guhao.fancy_trail.client.pipeline.PostEffectPipelines.shaderOrthoMatrix;

public class StarrySwordTrail extends PostPassBase {
    public StarrySwordTrail(String resourceLocation, ResourceManager resmgr) throws IOException {
        super(resourceLocation, resmgr);
    }

    public void process(RenderTarget inTarget, RenderTarget mask, RenderTarget outTarget,
                        int starTextureId, float time, float intensity, float starScale,
                        float opacity, int layers) {
        prevProcess(inTarget, outTarget);
        inTarget.unbindWrite();

        RenderSystem.viewport(0, 0, outTarget.width, outTarget.height);
        this.effect.setSampler("DiffuseSampler", inTarget::getColorTextureId);
        this.effect.setSampler("Mask", mask::getColorTextureId);
        this.effect.setSampler("StarTexture", () -> starTextureId);

        this.effect.safeGetUniform("ProjMat").set(shaderOrthoMatrix);
        this.effect.safeGetUniform("OutSize").set((float) outTarget.width, (float) outTarget.height);
        this.effect.safeGetUniform("Time").set(time);
        this.effect.safeGetUniform("Intensity").set(intensity);
        this.effect.safeGetUniform("StarScale").set(starScale);
        this.effect.safeGetUniform("Opacity").set(opacity);
        this.effect.safeGetUniform("Layers").set(layers);

        this.effect.apply();
        pushVertex(inTarget, outTarget);
        this.effect.clear();
        outTarget.unbindWrite();
        inTarget.unbindRead();
    }

    // 简化版本
    public void process(RenderTarget inTarget, RenderTarget mask, RenderTarget outTarget, float time) {
        process(inTarget, mask, outTarget, 0, time, 1.0f, 4.0f, 0.8f, 12);
    }
}