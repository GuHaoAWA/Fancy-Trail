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

public class ChromaticAberrationRenderType extends PostParticleRenderType {

    static final PostEffectPipelines.Pipeline ppl =
            new Pipeline(OjangUtils.newRL(FT.MODID, "chromatic_aberration_o"), 1);

    private final float offsetR;
    private final float offsetG;
    private final float offsetB;
    private final float modulateR;
    private final float modulateG;
    private final float modulateB;

    public ChromaticAberrationRenderType(ResourceLocation name, ResourceLocation location,
                                         float offsetR, float offsetG, float offsetB,
                                         float modulateR, float modulateG, float modulateB) {
        super(name, location);
        this.offsetR = offsetR;
        this.offsetG = offsetG;
        this.offsetB = offsetB;
        this.modulateR = modulateR;
        this.modulateG = modulateG;
        this.modulateB = modulateB;
        priority = 1;
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
        private static final ResourceLocation tmpTarget = OjangUtils.newRL(FT.MODID, "chromatic_aberration_tmp");

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

        void handleChromaticEffect(RenderTarget src) {
            RenderTarget tmp = TargetManager.getTarget(tmpTarget);
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();


            FTPostPasses.chromatic_aberration.process(
                    main,    // 输入：主渲染目标
                    src,     // 输入：粒子渲染目标
                    tmp,     // 输出：临时目标
                    FTClientConfig.getChromaticEffect()
            );

            FTPostPasses.blit.process(tmp, main);
            TargetManager.ReleaseTarget(tmpTarget);
        }

        @Override
        public void PostEffectHandler() {
            handleChromaticEffect(bufferTarget);
        }
    }

    // 便捷构造方法
    public static ChromaticAberrationRenderType createDefault(ResourceLocation name, ResourceLocation location) {
        return new ChromaticAberrationRenderType(
                name, location,
                1.005f, 1.0f, 1.003f,  // 默认RGB偏移
                1.0f, 1.0f, 1.0f       // 默认颜色调制
        );
    }

    public static ChromaticAberrationRenderType createWithOffset(ResourceLocation name, ResourceLocation location,
                                                                 float offsetR, float offsetG, float offsetB) {
        return new ChromaticAberrationRenderType(
                name, location,
                offsetR, offsetG, offsetB,
                1.0f, 1.0f, 1.0f
        );
    }

    public static ChromaticAberrationRenderType createFull(ResourceLocation name, ResourceLocation location,
                                                           float offsetR, float offsetG, float offsetB,
                                                           float modulateR, float modulateG, float modulateB) {
        return new ChromaticAberrationRenderType(
                name, location,
                offsetR, offsetG, offsetB,
                modulateR, modulateG, modulateB
        );
    }

    // Getter方法
    public float getOffsetR() { return offsetR; }
    public float getOffsetG() { return offsetG; }
    public float getOffsetB() { return offsetB; }
    public float getModulateR() { return modulateR; }
    public float getModulateG() { return modulateG; }
    public float getModulateB() { return modulateB; }
}