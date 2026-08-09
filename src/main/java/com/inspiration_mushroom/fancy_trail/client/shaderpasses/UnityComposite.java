package com.inspiration_mushroom.fancy_trail.client.shaderpasses;

import com.guhao.vix.client.shaderpasses.PostPassBase;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import static com.guhao.vix.client.pipeline.PostEffectPipelines.shaderOrthoMatrix;

public class UnityComposite extends PostPassBase {
    private static final float DEFAULT_BLOOM_INTENSITY = 5.0f;

    public UnityComposite(EffectInstance effect) {
        super(effect);
    }

    public UnityComposite(String resourceLocation, ResourceManager resmgr) throws IOException {
        super(resourceLocation, resmgr);
    }

    public void process(RenderTarget inTarget, RenderTarget outTarget, RenderTarget downTexture, RenderTarget bg) {
        process(inTarget, outTarget, downTexture, bg, DEFAULT_BLOOM_INTENSITY);
    }

    /**
     * 带泛光强度的版本。intensity < 0 时使用 JSON 默认值（5.0）。
     */
    public void process(RenderTarget inTarget, RenderTarget outTarget, RenderTarget downTexture,
                        RenderTarget bg, float bloomIntensity) {
        prevProcess(inTarget, outTarget);
        inTarget.unbindWrite();

        RenderSystem.viewport(0, 0, outTarget.width, outTarget.height);
        this.effect.setSampler("DiffuseSampler", inTarget::getColorTextureId);

        this.effect.safeGetUniform("ProjMat").set(shaderOrthoMatrix);
        this.effect.safeGetUniform("OutSize").set((float) outTarget.width, (float) outTarget.height);

        float intensity = Float.isFinite(bloomIntensity) ? bloomIntensity : DEFAULT_BLOOM_INTENSITY;
        this.effect.safeGetUniform("BloomIntensive").set(intensity);

        effect.setSampler("DownTexture", downTexture::getColorTextureId);
        effect.setSampler("Background", bg::getColorTextureId);

        this.effect.apply();

        pushVertex(inTarget, outTarget);

        this.effect.clear();
        outTarget.unbindWrite();
        inTarget.unbindRead();
    }
}
