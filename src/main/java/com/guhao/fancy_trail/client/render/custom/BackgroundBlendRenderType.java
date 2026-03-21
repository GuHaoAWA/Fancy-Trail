package com.guhao.fancy_trail.client.render.custom;

import com.guhao.fancy_trail.FT;
import com.guhao.fancy_trail.client.pipeline.PostEffectPipelines;
import com.guhao.fancy_trail.client.pipeline.PostParticleRenderType;
import com.guhao.fancy_trail.client.targets.TargetManager;
import com.guhao.fancy_trail.register.FTPostPasses;
import com.guhao.fancy_trail.unit.OjangUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import static com.guhao.fancy_trail.client.pipeline.PostEffectPipelines.*;
import static net.minecraft.client.Minecraft.ON_OSX;

public class BackgroundBlendRenderType extends PostParticleRenderType {

    static final PostEffectPipelines.Pipeline ppl =
            new Pipeline(OjangUtils.newRL(FT.MODID, "background_blend"), 1000);

    private final float blendStrength;
    private final float glowIntensity;
    private final float alphaBoost;

    public BackgroundBlendRenderType(ResourceLocation name, ResourceLocation texture) {
        this(name, texture, 0.85f, 1.0f, 1.2f);
    }

    public BackgroundBlendRenderType(ResourceLocation name, ResourceLocation texture,
                                     float blendStrength, float glowIntensity, float alphaBoost) {
        super(name, texture);
        this.blendStrength = blendStrength;
        this.glowIntensity = glowIntensity;
        this.alphaBoost = alphaBoost;
        this.priority = 1000;

        ((Pipeline) ppl).setParameters(blendStrength, glowIntensity, alphaBoost);
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
        private static final ResourceLocation SCENE_COLOR_TARGET = OjangUtils.newRL(FT.MODID, "scene_color");
        private static final ResourceLocation TEMP_TARGET = OjangUtils.newRL(FT.MODID, "background_blend_temp");

        private float currentTime = 0.0f;
        private float blendStrength = 0.85f;
        private float glowIntensity = 1.0f;
        private float alphaBoost = 1.2f;

        public Pipeline(ResourceLocation name, int priority) {
            super(name);
            this.priority = priority;
        }

        public void setParameters(float blendStrength, float glowIntensity, float alphaBoost) {
            this.blendStrength = blendStrength;
            this.glowIntensity = glowIntensity;
            this.alphaBoost = alphaBoost;
        }

        @Override
        public void start() {
            if (started) {
                if (isActive() && bufferTarget != null) {
                    bufferTarget.bindWrite(false);
                }
            } else {
                if (bufferTarget == null) {
                    bufferTarget = TargetManager.getTarget(name);
                    if (bufferTarget != null) {
                        bufferTarget.clear(ON_OSX);
                    }
                }

                RenderTarget main = getSource();
                if (isActive() && bufferTarget != null && main != null) {
                    bufferTarget.copyDepthFrom(main);
                    PostEffectQueue.add(this);
                    bufferTarget.bindWrite(false);
                    started = true;
                }
            }
        }

        @Override
        public void suspend() {
            if (isActive() && bufferTarget != null) {
                bufferTarget.unbindWrite();
                bufferTarget.unbindRead();
                RenderTarget rt = getSource();
                if (rt != null) {
                    rt.bindWrite(false);
                }
            } else {
                RenderTarget rt = getSource();
                if (rt != null) {
                    rt.bindWrite(false);
                }
            }
        }
        void handleBackgroundBlend(RenderTarget src) {
            if (FTPostPasses.background_blend == null || src == null) return;

            RenderTarget temp = null;
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();

            if (main == null) return;

            try {
                temp = TargetManager.getTarget(TEMP_TARGET);
                if (temp == null) return;

                // 确保尺寸一致
                if (temp.width != main.width || temp.height != main.height) {
                    temp.resize(main.width, main.height, ON_OSX);
                }

                currentTime += 0.016f;
                if (currentTime > 1000) currentTime = 0;

                // 关键：传入 main（主场景）和 src（粒子目标）
                FTPostPasses.background_blend.process(
                        main,               // 主场景（背景）
                        src,                // 粒子纹理（刀光）
                        temp,               // 输出目标
                        blendStrength,
                        glowIntensity,
                        alphaBoost,
                        currentTime
                );

                // 将结果复制回主目标
                FTPostPasses.blit.process(temp, main);


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                TargetManager.ReleaseTarget(TEMP_TARGET);
            }
        }

        @Override
        public void PostEffectHandler() {
            if (bufferTarget != null) {
                handleBackgroundBlend(bufferTarget);
            }
        }
    }

    public static BackgroundBlendRenderType create(ResourceLocation texture) {
        return new BackgroundBlendRenderType(
                OjangUtils.newRL(FT.MODID, "background_blend_default"),
                texture
        );
    }

    public static BackgroundBlendRenderType createWithStrength(ResourceLocation texture,
                                                               float blendStrength,
                                                               float glowIntensity,
                                                               float alphaBoost) {
        return new BackgroundBlendRenderType(
                OjangUtils.newRL(FT.MODID, "background_blend_custom"),
                texture,
                blendStrength,
                glowIntensity,
                alphaBoost
        );
    }
}