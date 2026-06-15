package com.guhao.fancy_trail.client.shaderpasses;

import com.guhao.vix.client.shaderpasses.PostPassBase;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

import static com.guhao.vix.client.pipeline.PostEffectPipelines.shaderOrthoMatrix;


public class StaticAirDisturbance extends PostPassBase {
    public StaticAirDisturbance(String resourceLocation, ResourceManager resmgr) throws IOException {
        super(resourceLocation, resmgr);
    }

    public void process(RenderTarget inTarget, RenderTarget mask, RenderTarget outTarget,
                        float strength, float directionX, float directionY) {
        prevProcess(inTarget, outTarget);
        inTarget.unbindWrite();

        RenderSystem.viewport(0, 0, outTarget.width, outTarget.height);
        this.effect.setSampler("DiffuseSampler", inTarget::getColorTextureId);
        this.effect.setSampler("Mask", mask::getColorTextureId);

        this.effect.safeGetUniform("ProjMat").set(shaderOrthoMatrix);
        this.effect.safeGetUniform("OutSize").set((float) outTarget.width, (float) outTarget.height);
        this.effect.safeGetUniform("DistortionStrength").set(strength);
        this.effect.safeGetUniform("Direction").set(directionX, directionY);

        this.effect.apply();
        pushVertex(inTarget, outTarget);
        this.effect.clear();
        outTarget.unbindWrite();
        inTarget.unbindRead();
    }
}