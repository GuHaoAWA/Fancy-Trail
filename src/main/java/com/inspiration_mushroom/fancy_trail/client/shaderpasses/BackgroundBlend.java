package com.inspiration_mushroom.fancy_trail.client.shaderpasses;

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

    public void process(RenderTarget sceneTarget,     // main scene (background)
                        RenderTarget particleTarget,  // particle texture (blade trail)
                        RenderTarget outTarget,       // output target
                        float blendStrength,
                        float glowIntensity,
                        float alphaBoost,
                        float time) {
        prevProcess(sceneTarget, outTarget);
        sceneTarget.unbindWrite();

        RenderSystem.viewport(0, 0, outTarget.width, outTarget.height);

        // samplers - mirrors the AirDisturbance pattern
        this.effect.setSampler("DiffuseSampler", sceneTarget::getColorTextureId);      // main scene
        this.effect.setSampler("Mask", particleTarget::getColorTextureId);             // particle texture

        // uniforms
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