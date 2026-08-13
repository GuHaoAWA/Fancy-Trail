package com.inspiration_mushroom.fancy_trail.client.render.custom;

import com.inspiration_mushroom.fancy_trail.FT;
import com.inspiration_mushroom.fancy_trail.FTClientConfig;
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
    protected BufferBuilder setupBufferBuilder(Tesselator tesselator) {
        // Fixed-vix beginPost contract (1.21): the buffer-format seam RETURNS the builder.
        return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
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
                    main,    // input: main render target
                    src,     // input: particle render target
                    tmp,     // output: temp target
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

    // convenience factories
    public static ChromaticAberrationRenderType createDefault(ResourceLocation name, ResourceLocation location) {
        return new ChromaticAberrationRenderType(
                name, location,
                1.005f, 1.0f, 1.003f,  // default RGB offsets
                1.0f, 1.0f, 1.0f       // default color modulation
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

    // getters
    public float getOffsetR() { return offsetR; }
    public float getOffsetG() { return offsetG; }
    public float getOffsetB() { return offsetB; }
    public float getModulateR() { return modulateR; }
    public float getModulateG() { return modulateG; }
    public float getModulateB() { return modulateB; }
}