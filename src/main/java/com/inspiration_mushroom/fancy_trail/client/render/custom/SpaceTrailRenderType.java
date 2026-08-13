package com.inspiration_mushroom.fancy_trail.client.render.custom;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
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
    protected BufferBuilder setupBufferBuilder(Tesselator tesselator) {
        // Fixed-vix beginPost contract (1.21): the buffer-format seam RETURNS the builder.
        return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }
}
