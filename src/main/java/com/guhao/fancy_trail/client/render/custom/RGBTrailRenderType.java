package com.guhao.fancy_trail.client.render.custom;

import com.guhao.fancy_trail.FT;
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

public class RGBTrailRenderType extends PostParticleRenderType {

    static final PostEffectPipelines.Pipeline ppl =
            new Pipeline(OjangUtils.newRL(FT.MODID, "rgb_trail"), 1700);

    private final float intensity;
    private final float starScale;
    private final float rainbowSpeed;
    private final float rainbowScale;
    private final float rainbowMix;
    private final float opacity;

    public RGBTrailRenderType(ResourceLocation name, ResourceLocation location,
                              float intensity, float starScale,
                              float rainbowSpeed, float rainbowScale, float rainbowMix, float opacity) {
        super(name, location);
        this.intensity = intensity;
        this.starScale = starScale;
        this.rainbowSpeed = rainbowSpeed;
        this.rainbowScale = rainbowScale;
        this.rainbowMix = rainbowMix;
        this.opacity = opacity;
        priority = 1700;
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
        private static final ResourceLocation tmpTarget = OjangUtils.newRL(FT.MODID, "infinity_sword_trail_tmp");
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

        void handleInfinityEffect(RenderTarget src) {
            RenderTarget tmp = TargetManager.getTarget(tmpTarget);
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();

            currentTime += 0.05f;

            // 使用默认参数
            FTPostPasses.rgb_trail.process(
                    main,    // 输入：主渲染目标
                    src,     // 输入：粒子渲染目标
                    tmp,     // 输出：临时目标
                    currentTime,
                    1.0f,    // intensity
                    3.0f,    // starScale
                    0.08f,   // rainbowSpeed
                    1.2f,    // rainbowScale
                    0.8f,    // rainbowMix
                    0.7f     // opacity
            );

            FTPostPasses.blit.process(tmp, main);
            TargetManager.ReleaseTarget(tmpTarget);
        }

        @Override
        public void PostEffectHandler() {
            handleInfinityEffect(bufferTarget);
        }
    }

    // 便捷构造方法
    public static RGBTrailRenderType createDefault(ResourceLocation name, ResourceLocation location) {
        return new RGBTrailRenderType(
                name, location,
                1.0f, 3.0f, 0.08f, 1.2f, 0.8f, 0.7f
        );
    }

    public static RGBTrailRenderType createStrong(ResourceLocation name, ResourceLocation location) {
        return new RGBTrailRenderType(
                name, location,
                1.5f, 4.0f, 0.12f, 1.5f, 0.9f, 0.9f
        );
    }
}