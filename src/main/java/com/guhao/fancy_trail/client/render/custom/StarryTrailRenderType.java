package com.guhao.fancy_trail.client.render.custom;

import com.guhao.fancy_trail.FT;
import com.guhao.fancy_trail.FTClientConfig;
import com.guhao.fancy_trail.client.pipeline.PostEffectPipelines;
import com.guhao.fancy_trail.client.pipeline.PostParticleRenderType;
import com.guhao.fancy_trail.client.targets.TargetManager;
import com.guhao.fancy_trail.register.FTPostPasses;
import com.guhao.fancy_trail.unit.OjangUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import static com.guhao.fancy_trail.client.pipeline.PostEffectPipelines.*;
import static net.minecraft.client.Minecraft.ON_OSX;

public class StarryTrailRenderType extends PostParticleRenderType {

    static final PostEffectPipelines.Pipeline ppl =
            new Pipeline(OjangUtils.newRL(FT.MODID, "starry_trail"), 1600);

    private final float intensity;
    private final float starScale;
    private final float opacity;
    private final int layers;
    private final ResourceLocation starTexture;

    public StarryTrailRenderType(ResourceLocation name, ResourceLocation location,
                                 float intensity, float starScale, float opacity, int layers,
                                 ResourceLocation starTexture) {
        super(name, location);
        this.intensity = intensity;
        this.starScale = starScale;
        this.opacity = opacity;
        this.layers = layers;
        this.starTexture = starTexture;
        priority = 1600;
    }

    @Override
    protected ShaderInstance getShader() {
        return GameRenderer.particleShader;
    }

    @Override
    public void setupBufferBuilder(BufferBuilder bufferBuilder) {
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public PostEffectPipelines.Pipeline getPipeline() {
        return ppl;
    }

    public static class Pipeline extends PostEffectPipelines.Pipeline {
        private static final ResourceLocation tmpTarget = OjangUtils.newRL(FT.MODID, "starry_trail_tmp");
        private float currentTime = 0.0f;

        public Pipeline(ResourceLocation name, int priority) {
            super(name);
            this.priority = priority;
        }

        @Override
        public void start() {
            if (started) {
                if (isActive()) {
                    bufferTarget.bindWrite(false);
                }
            } else {
                if (bufferTarget == null) {
                    bufferTarget = TargetManager.getTarget(name);
                    bufferTarget.clear(ON_OSX);
                }

                RenderTarget main = getSource();
                if (isActive()) {
                    bufferTarget.copyDepthFrom(main);
                    PostEffectQueue.add(this);
                    bufferTarget.bindWrite(false);
                    started = true;
                }
            }
        }

        @Override
        public void suspend() {
            if (isActive()) {
                bufferTarget.unbindWrite();
                bufferTarget.unbindRead();
                RenderTarget rt = getSource();
                rt.bindWrite(false);
            } else {
                getSource().bindWrite(false);
            }
        }
        void handleStarryEffect(RenderTarget src) {
            RenderTarget tmp = TargetManager.getTarget(tmpTarget);
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();

            currentTime += 0.05f;

            // 获取实例参数
            StarryTrailRenderType renderType = getStarryRenderType();
            float intensity = renderType != null ? renderType.intensity : FTClientConfig.getIntensity();
            float starScale = renderType != null ? renderType.starScale : FTClientConfig.getStarScale();
            float opacity = renderType != null ? renderType.opacity : FTClientConfig.getOpacity();
            int layers = renderType != null ? renderType.layers : FTClientConfig.getLayers();

            ResourceLocation starTexture = getValidStarTexture(renderType);

            // 获取贴图ID
            int starTextureId = Minecraft.getInstance().getTextureManager().getTexture(starTexture).getId();

            FTPostPasses.starry_sword_trail.process(
                    main,           // 输入：主渲染目标
                    src,            // 输入：粒子渲染目标
                    tmp,            // 输出：临时目标
                    starTextureId,  // 星星贴图ID
                    currentTime,
                    intensity,
                    starScale,
                    opacity,
                    layers
            );

            FTPostPasses.blit.process(tmp, main);
            TargetManager.ReleaseTarget(tmpTarget);
        }

        private StarryTrailRenderType getStarryRenderType() {
            // 这里需要根据实际架构获取当前实例
            // 暂时返回null，使用默认参数
            return null;
        }

        private ResourceLocation getDefaultStarTexture() {
            // 默认星星贴图
            return ResourceLocation.fromNamespaceAndPath(FT.MODID, "textures/effect/star.png");
        }

        @Override
        public void PostEffectHandler() {
            handleStarryEffect(bufferTarget);
        }
    }


    public static StarryTrailRenderType createSubtle(ResourceLocation name, ResourceLocation location, ResourceLocation starTexture) {
        return new StarryTrailRenderType(
                name, location,
                0.4f, 1.2f, 0.16f, 2,
//                intensity,scale,opacity,layers,
                starTexture
        );
    }



    // 自定义参数构造方法
    public static StarryTrailRenderType createCustom(ResourceLocation name, ResourceLocation location,
                                                     float intensity, float starScale, float opacity, int layers,
                                                     ResourceLocation starTexture) {
        return new StarryTrailRenderType(
                name, location,
                intensity, starScale, opacity, layers,
                starTexture
        );
    }
    private static ResourceLocation getValidStarTexture(StarryTrailRenderType renderType) {
        if (renderType != null && renderType.starTexture != null) {
            return renderType.starTexture;
        }
        return FTClientConfig.getStarTexture();
    }
}