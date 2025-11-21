package com.guhao.fancy_trail.client.shaderpasses;



import com.guhao.fancy_trail.client.pipeline.PostEffectPipelines;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
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
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(0.0D, 0.0D, 700).endVertex();
        bufferbuilder.vertex(inTarget.width, 0.0D, 700).endVertex();
        bufferbuilder.vertex(inTarget.width, inTarget.height, 700).endVertex();
        bufferbuilder.vertex(0.0D, inTarget.height, 700).endVertex();
        RenderSystem.depthFunc(515);
        this.effect.clear();
        outTarget.unbindWrite();
        inTarget.unbindRead();
    }
}
