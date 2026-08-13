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

public class AfterImageRenderType extends PostParticleRenderType {

    static final PostEffectPipelines.Pipeline ppl =
            new Pipeline(OjangUtils.newRL(FT.MODID, "after_image"), 200);

    private final float strength;
    private final float decaySpeed;
    private final float motionBlurFactor;
    private final float colorShift;
    private final float directionX;
    private final float directionY;

    public AfterImageRenderType(ResourceLocation name, ResourceLocation texture,
                                float strength, float decaySpeed,
                                float motionBlurFactor, float colorShift,
                                float directionX, float directionY) {
        super(name, texture);
        this.strength = strength;
        this.decaySpeed = decaySpeed;
        this.motionBlurFactor = motionBlurFactor;
        this.colorShift = colorShift;
        this.directionX = directionX;
        this.directionY = directionY;
        priority = 900;
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
        private static final ResourceLocation tmpTarget = OjangUtils.newRL(FT.MODID, "after_image_tmp");

        private float currentTime = 0.0f;
        private float[] previousFrameBuffer; // stores the previous frame for the after-image effect
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

        void handleAfterImageEffect(RenderTarget src) {
            RenderTarget tmp = TargetManager.getTarget(tmpTarget);
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();

            updateAnimation();

            // scale after-image strength with movement speed
            float dynamicStrength = calculateDynamicStrength();
            float dynamicDecay = calculateDynamicDecay();

            FTPostPasses.afterImage.process(
                    main,    // input: main render target
                    src,     // input: particle render target
                    tmp,     // output: temp target
                    dynamicStrength,
                    currentTime,
                    dynamicDecay,
                    getMotionBlurFactor(),
                    getColorShift(),
                    getDirectionX(),
                    getDirectionY()
            );

            FTPostPasses.blit.process(tmp, main);
            TargetManager.ReleaseTarget(tmpTarget);
        }

        private void updateAnimation() {
            if (animationStarted) {
                currentTime += 0.016f; // ~60fps time increment
            }
        }

        private float calculateDynamicStrength() {
            // pulse the after-image strength over time (breathing effect)
            float pulse = 0.8f + 0.2f * (float)Math.sin(currentTime * 3.0f);
            return 0.3f * pulse; // base strength 0.3 with pulse
        }

        private float calculateDynamicDecay() {
            // dynamic decay speed
            return 2.0f + (float)Math.sin(currentTime * 2.0f) * 0.5f;
        }

        private float getMotionBlurFactor() {
            return 0.6f; // motion blur strength
        }

        private float getColorShift() {
            return 0.1f * (float)Math.sin(currentTime * 2.5f); // dynamic color shift
        }

        private float getDirectionX() {
            return (float)Math.cos(currentTime * 2.0f); // dynamic direction
        }

        private float getDirectionY() {
            return (float)Math.sin(currentTime * 2.0f);
        }

        @Override
        public void PostEffectHandler() {
            handleAfterImageEffect(bufferTarget);
        }
    }
}