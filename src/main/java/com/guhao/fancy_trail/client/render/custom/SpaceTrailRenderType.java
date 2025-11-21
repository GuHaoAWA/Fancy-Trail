package com.guhao.fancy_trail.client.render.custom;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

public class SpaceTrailRenderType extends SpaceBrokenRenderType {
    public SpaceTrailRenderType(ResourceLocation name, ResourceLocation texture, int layer, int vertexCount) {
        super(name, texture, layer, vertexCount);
    }

    @Override
    protected ShaderInstance getShader() {
        return GameRenderer.particleShader;
    }

    @Override
    public void setupBufferBuilder(BufferBuilder bufferBuilder) {
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }
}
