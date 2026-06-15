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


public class StaticAirDisturbanceRenderType extends PostParticleRenderType {

    static final PostEffectPipelines.Pipeline ppl =
            new Pipeline(OjangUtils.newRL(FT.MODID, "static_air_disturbance"), 150);

    public StaticAirDisturbanceRenderType(ResourceLocation name, ResourceLocation location) {
        super(name, location);
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
        private static final ResourceLocation tmpTarget = OjangUtils.newRL(FT.MODID, "static_air_disturbance_tmp");

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

        private float lastStrength = 0.0f;

        void handleDisturbanceEffect(RenderTarget src) {
            RenderTarget tmp = TargetManager.getTarget(tmpTarget);
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();

            float baseStrength = 0.007f;
            float speedFactor = 1.0f;

            if (Minecraft.getInstance().player != null) {
                float speed = (float) Minecraft.getInstance().player.getDeltaMovement().length();
                speedFactor = 0.6f + Math.min(0.8f, speed * 1.5f);
            }

            float targetStrength = baseStrength * 1.0f * speedFactor;
            lastStrength = lastStrength * 0.7f + targetStrength * 0.3f;

            float directionX = 1.0f;
            float directionY = 0.3f;
            if (Minecraft.getInstance().player != null) {
                float yaw = Minecraft.getInstance().player.getYRot();
                directionX = (float) Math.cos(Math.toRadians(yaw));
                directionY = (float) Math.sin(Math.toRadians(yaw)) * 0.5f;
            }

            FTPostPasses.static_air_disturbance.process(
                    main, src, tmp,
                    lastStrength,
                    directionX,
                    directionY
            );

            FTPostPasses.blit.process(tmp, main);
            TargetManager.ReleaseTarget(tmpTarget);
        }

        @Override
        public void PostEffectHandler() {
            handleDisturbanceEffect(bufferTarget);
        }
    }
}