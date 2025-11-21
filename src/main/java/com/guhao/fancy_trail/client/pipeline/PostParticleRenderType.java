package com.guhao.fancy_trail.client.pipeline;


import com.guhao.fancy_trail.register.FTPostPasses;
import com.guhao.fancy_trail.unit.RenderUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z;
import static net.minecraft.client.Minecraft.ON_OSX;
import static org.lwjgl.opengl.ARBInternalformatQuery2.GL_TEXTURE_2D;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS;

@SuppressWarnings("removal")
public abstract class PostParticleRenderType implements ParticleRenderType {
    static ResourceLocation tempTarget = new ResourceLocation("ft:depth_cull_temp");
    protected final ResourceLocation renderTypeID;
    protected final ResourceLocation texture;

    //protected boolean started = false;
    public int priority = 0;

    public PostParticleRenderType(ResourceLocation renderTypeID, ResourceLocation texture) {
        this.renderTypeID = renderTypeID;
        this.texture = texture;
    }

    public static RenderTarget createTempTarget(RenderTarget screenTarget) {
        RenderTarget rendertarget = new TextureTarget(screenTarget.width, screenTarget.height, true, ON_OSX);
        rendertarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        rendertarget.clear(ON_OSX);
        //if (screenTarget.isStencilEnabled()) { rendertarget.enableStencil(); }
        return rendertarget;
    }

    public static void Blit(RenderTarget source, RenderTarget output) {
        //RenderTarget blur = createTempTarget(source);
        //RenderTarget blur2 = createTempTarget(source);

        FTPostPasses.blit.process(source, output,
                (effect) ->
                {
                }
        );

        //blur1.destroyBuffers();
        //blur2.destroyBuffers();
    }

    @Override
    public void begin(@NotNull BufferBuilder bufferBuilder, @NotNull TextureManager textureManager) {
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(this::getShader);

        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -1);
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);

        if (texture != null) RenderUtils.GLSetTexture(texture);
        getPipeline().start();
        setupBufferBuilder(bufferBuilder);
    }

    protected ShaderInstance getShader() {
        return GameRenderer.positionColorTexLightmapShader;
    }

    public void callPipeline() {
        getPipeline().call();
    }

    public boolean tryCallPipeline() {
        if (!PostEffectPipelines.isActive()) {
            callPipeline();
            return true;
        } else return false;
    }

    //simple_post_particle
    @Override
    public void end(Tesselator tesselator) {
        tesselator.getBuilder().setQuadSorting(ORTHOGRAPHIC_Z);
        tesselator.end();
        getPipeline().suspend();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
    }

    public void setupBufferBuilder(BufferBuilder bufferBuilder) {
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
    }

    public abstract PostEffectPipelines.Pipeline getPipeline();

    public String toString() {
        return renderTypeID.toString();
    }
}
