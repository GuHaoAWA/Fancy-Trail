package com.guhao.fancy_trail.client.render.custom;



import com.guhao.fancy_trail.FT;
import com.guhao.fancy_trail.client.pipeline.PostEffectPipelines;
import com.guhao.fancy_trail.client.pipeline.PostParticleRenderType;
import com.guhao.fancy_trail.client.targets.TargetManager;
import com.guhao.fancy_trail.unit.OjangUtils;
import com.guhao.fancy_trail.unit.RenderUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
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

    @Override
    public void begin(@NotNull BufferBuilder bufferBuilder, @NotNull TextureManager textureManager) {
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(this::getShader);

        if (texture != null) RenderUtils.GLSetTexture(texture);
        getPipeline().start();
        setupBufferBuilder(bufferBuilder);
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
    public void setupBufferBuilder(BufferBuilder bufferBuilder) {
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
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
