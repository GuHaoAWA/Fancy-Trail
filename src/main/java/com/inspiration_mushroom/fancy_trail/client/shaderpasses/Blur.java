package com.inspiration_mushroom.fancy_trail.client.shaderpasses;

import com.guhao.vix.client.pipeline.PostEffectPipelines;
import com.guhao.vix.client.shaderpasses.PostPassBase;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class Blur extends PostPassBase {
    public Blur(ResourceManager rsmgr) throws IOException {
        super(new EffectInstance(rsmgr, "fancy_trail:blur"));
    }


    public void process(RenderTarget inTarget, RenderTarget outTarget, float blurDirX, float blurDirY, int radius) {
        inTarget.unbindWrite();
        RenderSystem.viewport(0, 0, outTarget.width, outTarget.height);
        this.effect.setSampler("DiffuseSampler", inTarget::getColorTextureId);

        //Matrix4f shaderOrthoMatrix = Matrix4f.orthographic(0.0F, outTarget.width, outTarget.height, 0.0F, 0.1F, 1000.0F);
        this.effect.safeGetUniform("ProjMat").set(PostEffectPipelines.shaderOrthoMatrix);
        this.effect.safeGetUniform("OutSize").set((float) outTarget.width, (float) outTarget.height);
        this.effect.safeGetUniform("BlurDir").set(blurDirX, blurDirY);
        this.effect.safeGetUniform("Radius").set(radius);

        this.effect.apply();

        outTarget.clear(Minecraft.ON_OSX);
        outTarget.bindWrite(false);
        RenderSystem.depthFunc(519);
        // 1.21 builder API: Tesselator.begin RETURNS the builder; vertex()/endVertex() ->
        // addVertex(); the batch must be explicitly uploaded (BufferUploader.draw) — the 1.20.1
        // original never drew this buffer itself, the next begin() flushed it implicitly.
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.addVertex(0.0F, 0.0F, 700.0F);
        bufferbuilder.addVertex((float) inTarget.width, 0.0F, 700.0F);
        bufferbuilder.addVertex((float) inTarget.width, (float) inTarget.height, 700.0F);
        bufferbuilder.addVertex(0.0F, (float) inTarget.height, 700.0F);
        BufferUploader.draw(bufferbuilder.buildOrThrow());
        RenderSystem.depthFunc(515);
        this.effect.clear();
        outTarget.unbindWrite();
        inTarget.unbindRead();
    }
}
