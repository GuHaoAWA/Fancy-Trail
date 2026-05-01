package com.guhao.fancy_trail.client.render.custom;

import com.guhao.fancy_trail.FT;
import com.guhao.fancy_trail.register.FTPostPasses;
import com.guhao.vix.client.pipeline.PostEffectPipelines;
import com.guhao.vix.client.pipeline.PostParticleRenderType;
import com.guhao.vix.client.targets.TargetManager;
import com.guhao.vix.util.OjangUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.BufferBuilder;
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
    public void setupBufferBuilder(BufferBuilder bufferBuilder) {
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public PostEffectPipelines.Pipeline getPipeline() {
        return ppl;
    }

    public static class Pipeline extends PostEffectPipelines.Pipeline {
        private static final ResourceLocation tmpTarget = OjangUtils.newRL(FT.MODID, "after_image_tmp");

        private float currentTime = 0.0f;
        private float[] previousFrameBuffer; // 存储上一帧用于残影效果
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

            // 根据运动速度动态调整残影强度
            float dynamicStrength = calculateDynamicStrength();
            float dynamicDecay = calculateDynamicDecay();

            FTPostPasses.afterImage.process(
                    main,    // 输入：主渲染目标
                    src,     // 输入：粒子渲染目标
                    tmp,     // 输出：临时目标
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
                currentTime += 0.016f; // 约60fps的增量
            }
        }

        private float calculateDynamicStrength() {
            // 根据时间动态调整残影强度，产生呼吸效果
            float pulse = 0.8f + 0.2f * (float)Math.sin(currentTime * 3.0f);
            return 0.3f * pulse; // 基础强度0.3，带脉冲效果
        }

        private float calculateDynamicDecay() {
            // 动态衰减速度
            return 2.0f + (float)Math.sin(currentTime * 2.0f) * 0.5f;
        }

        private float getMotionBlurFactor() {
            return 0.6f; // 运动模糊强度
        }

        private float getColorShift() {
            return 0.1f * (float)Math.sin(currentTime * 2.5f); // 动态色彩偏移
        }

        private float getDirectionX() {
            return (float)Math.cos(currentTime * 2.0f); // 动态方向
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