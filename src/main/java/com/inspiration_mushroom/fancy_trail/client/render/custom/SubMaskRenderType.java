package com.inspiration_mushroom.fancy_trail.client.render.custom;



import com.inspiration_mushroom.fancy_trail.FT;
import com.guhao.vix.client.pipeline.PostEffectPipelines;
import com.guhao.vix.client.pipeline.PostParticleRenderType;
import com.guhao.vix.client.targets.TargetManager;
import com.guhao.vix.util.OjangUtils;
import com.guhao.vix.util.RenderUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.client.Minecraft.ON_OSX;

public class SubMaskRenderType extends PostParticleRenderType {
    public static final PPL ppl = new PPL(OjangUtils.newRL(FT.MODID, "sub_mask"));

    public SubMaskRenderType(ResourceLocation renderTypeID, ResourceLocation texture) {
        super(renderTypeID, texture);
    }

    // Upstream overrode the FULL begin() (its only delta vs the vix base: culling ENABLED).
    // Fixed-vix beginPost contract (1.21): the interface begin() must stay the inherited
    // side-effect-free no-op (a side-effectful begin() re-creates the FBO leak through the
    // NeoForge vanilla particle loop), so the upstream begin() body lives in beginPost(),
    // which the vix MixinParticleEngine always pairs with finish().
    @Override
    public BufferBuilder beginPost(@NotNull Tesselator tesselator, @NotNull TextureManager textureManager) {
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(this::getShader);

        if (texture != null) RenderUtils.GLSetTexture(texture);
        getPipeline().start();
        return setupBufferBuilder(tesselator);
    }

    public RenderTarget getTarget() {
        if (ppl.getBufferTarget() == null) {
            var ret = TargetManager.getTarget(ppl.name);
            ret.setClearColor(0, 0, 0, 1.f);
            ret.clear(ON_OSX);
            return ret;
        }
        return ppl.getBufferTarget();
    }

    @Override
    protected BufferBuilder setupBufferBuilder(Tesselator tesselator) {
        // Fixed-vix beginPost contract (1.21): the buffer-format seam RETURNS the builder.
        return tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
    }

    @Override
    public PostEffectPipelines.Pipeline getPipeline() {
        return ppl;
    }

    public static class PPL extends PostEffectPipelines.Pipeline {
        private static final ResourceLocation tmpTarget
                = OjangUtils.newRL(FT.MODID, "sub_mask_tmp");

        public PPL(ResourceLocation name) {
            super(name);
            priority = 1000;
        }

        public RenderTarget getBufferTarget() {
            return bufferTarget;
        }

        @Override
        public void PostEffectHandler() {
            //RenderTarget tmp = TargetManager.getTarget(tmpTarget);
            //RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
            //doDepthCull(src, depth);
            //System.out.println("Handle");
            //FTPostPasses.black_hole.process(main, bufferTarget, tmp);
            //FTPostPasses.blit.process(tmp, main);
            // TargetManager.ReleaseTarget(tmpTarget);
        }
    }


}
