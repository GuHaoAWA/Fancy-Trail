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

public class AirDisturbanceRenderType extends PostParticleRenderType {

    static final PostEffectPipelines.Pipeline ppl =
            new Pipeline(OjangUtils.newRL(FT.MODID, "air_disturbance"), 150);

    private final float strength;
    private final float speed;
    private final float directionX;
    private final float directionY;
    private final float bladeLength;

    public AirDisturbanceRenderType(ResourceLocation name,ResourceLocation location, float strength, float speed,
                                    float directionX, float directionY, float bladeLength) {
        super(name, location);
        this.strength = strength;
        this.speed = speed;
        this.directionX = directionX;
        this.directionY = directionY;
        this.bladeLength = bladeLength;
        priority = 1000;
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

                    // 开始动画
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

        void handleDisturbanceEffect(RenderTarget src) {
            RenderTarget tmp = TargetManager.getTarget(tmpTarget);
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();

            updateAnimation();

            float strength = 0.15f;
            float speed = 3.0f;
            float directionX = 1.0f;
            float directionY = 0.0f;
            float bladeLength = 0.5f;


            FTPostPasses.air_disturbance.process(
                    main,    // 输入：主渲染目标
                    src,     // 输入：粒子渲染目标
                    tmp,     // 输出：临时目标
                    strength * getCurrentStrength(),
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
                currentTime += 0.05f; // 每帧增加
                progress = calculateProgress();

                // 动画结束后重置
                if (progress <= 0.0f) {
                    animationStarted = false;
                }
            }
        }

        private float calculateProgress() {
            float duration = 2.0f; // 总持续时间（秒）
            float normalizedTime = currentTime / duration;

            if (normalizedTime < 0.3f) {
                // 快速出现 (0-30%)
                return normalizedTime / 0.3f;
            } else if (normalizedTime < 1.0f) {
                // 缓慢消失 (30-100%)
                return 1.0f - ((normalizedTime - 0.3f) / 0.7f);
            } else {
                // 结束
                return 0.0f;
            }
        }

        private float getCurrentStrength() {
            float normalizedTime = currentTime / 2.0f; // 2秒总时长

            if (normalizedTime > 0.5f) {
                // 后半段衰减
                float fade = 1.0f - ((normalizedTime - 0.5f) / 0.5f);
                return fade * fade; // 二次衰减
            }
            return 1.0f;
        }

        @Override
        public void PostEffectHandler() {
            handleDisturbanceEffect(bufferTarget);
        }
    }
}