package com.inspiration_mushroom.fancy_trail.client.render.custom;


import com.inspiration_mushroom.fancy_trail.FT;
import com.inspiration_mushroom.fancy_trail.register.FTPostPasses;
import com.guhao.vix.client.pipeline.PostEffectPipelines;
import com.guhao.vix.client.pipeline.PostParticleRenderType;
import com.guhao.vix.client.targets.TargetManager;
import com.guhao.vix.util.OjangUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import static com.guhao.vix.client.pipeline.PostEffectPipelines.*;
import static net.minecraft.client.Minecraft.ON_OSX;


public class AirDisturbanceRenderType extends PostParticleRenderType {

    static final PostEffectPipelines.Pipeline ppl =
            new Pipeline(OjangUtils.newRL(FT.MODID, "air_disturbance"), 150);

    public AirDisturbanceRenderType(ResourceLocation name, ResourceLocation location) {
        super(name, location);
        priority = 1000;
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

    @Override
    public PostEffectPipelines.Pipeline getPipeline() {
        return ppl;
    }

    public static class Pipeline extends PostEffectPipelines.Pipeline {
        private static final ResourceLocation tmpTarget = OjangUtils.newRL(FT.MODID, "air_disturbance_tmp");

        private float currentTime = 0.0f;
        private float progress = 0.0f;
        private boolean animationStarted = false;

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

                    if (!animationStarted) {
                        currentTime = 0.0f;
                        progress = 0.0f;
                        animationStarted = true;
                    }
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

        private float lastStrength = 0.0f;

        void handleDisturbanceEffect(RenderTarget src) {
            RenderTarget tmp = TargetManager.getTarget(tmpTarget);
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();

            updateAnimation();

            float baseStrength = 0.007f;
            float speedFactor = 1.0f;

            if (Minecraft.getInstance().player != null) {
                float speed = (float) Minecraft.getInstance().player.getDeltaMovement().length();
                speedFactor = 0.6f + Math.min(0.8f, speed * 1.5f);
            }

            float targetStrength = baseStrength * getCurrentStrength() * speedFactor;
            lastStrength = lastStrength * 0.7f + targetStrength * 0.3f;

            float directionX = 1.0f;
            float directionY = 0.3f;
            if (Minecraft.getInstance().player != null) {
                float yaw = Minecraft.getInstance().player.getYRot();
                directionX = (float) Math.cos(Math.toRadians(yaw));
                directionY = (float) Math.sin(Math.toRadians(yaw)) * 0.5f;
            }

            float bladeLength = 0.65f * (0.8f + progress * 0.4f);

            FTPostPasses.air_disturbance.process(
                    main, src, tmp,
                    lastStrength,
                    currentTime,
                    progress,
                    directionX,
                    directionY,
                    bladeLength
            );

            FTPostPasses.blit.process(tmp, main);
            TargetManager.ReleaseTarget(tmpTarget);
        }

        private void updateAnimation() {
            if (animationStarted) {
                currentTime += 0.05f;
                progress = calculateProgress();

                if (progress <= 0.0f) {
                    animationStarted = false;
                }
            }
        }

        private float calculateProgress() {
            float duration = 2.0f;
            float normalizedTime = currentTime / duration;

            if (normalizedTime < 0.3f) {
                return normalizedTime / 0.3f;
            } else if (normalizedTime < 1.0f) {
                return 1.0f - ((normalizedTime - 0.3f) / 0.7f);
            } else {
                return 0.0f;
            }
        }

        private float getCurrentStrength() {
            float normalizedTime = currentTime / 2.0f;

            if (normalizedTime > 0.5f) {
                float fade = 1.0f - ((normalizedTime - 0.5f) / 0.5f);
                return fade * fade;
            }
            return 1.0f;
        }

        @Override
        public void PostEffectHandler() {
            handleDisturbanceEffect(bufferTarget);
        }
    }
}