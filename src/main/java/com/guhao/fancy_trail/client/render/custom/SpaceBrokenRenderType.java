package com.guhao.fancy_trail.client.render.custom;


import com.guhao.fancy_trail.FT;
import com.guhao.fancy_trail.client.pipeline.PostEffectPipelines;
import com.guhao.fancy_trail.client.pipeline.PostParticleRenderType;
import com.guhao.fancy_trail.client.targets.TargetManager;
import com.guhao.fancy_trail.register.FTPostPasses;
import com.guhao.fancy_trail.unit.OjangUtils;
import com.guhao.fancy_trail.unit.RenderUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import static com.guhao.fancy_trail.client.pipeline.PostEffectPipelines.*;
import static net.minecraft.client.Minecraft.ON_OSX;

public class SpaceBrokenRenderType extends PostParticleRenderType {

    static final PostEffectPipelines.Pipeline ppl1 =
            new Pipeline(OjangUtils.newRL(FT.MODID, "mesh_space_broken_0"), 10);
    static final PostEffectPipelines.Pipeline ppl2 =
            new Pipeline(OjangUtils.newRL(FT.MODID, "mesh_space_broken_1"), 11);
    final int layer;
    final int vertex;

    public SpaceBrokenRenderType(ResourceLocation name, ResourceLocation texture, int layer, int vertexCount) {
        super(name, texture);
        this.layer = layer;
        vertex = vertexCount;
        priority = 1000;
    }


    public SpaceBrokenRenderType(ResourceLocation name, int layer) {
        super(name, RenderUtils.GetTexture("particle/sparks"));
        this.layer = layer;
        vertex = 3;
        priority = 1000;
    }

    @Override
    public void setupBufferBuilder(BufferBuilder bufferBuilder) {
        bufferBuilder.begin(vertex == 3 ? VertexFormat.Mode.TRIANGLES : VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
    }

    @Override
    public PostEffectPipelines.Pipeline getPipeline() {
        return layer == 0 ? ppl1 : ppl2;
    }

    public static class Pipeline extends PostEffectPipelines.Pipeline {
        //private ResourceLocation space_broken_mask = OjangUtils.newRL(FT.MODID, "space_broken_mask_" + priority);
        private static final ResourceLocation tmpTarget = OjangUtils.newRL(FT.MODID, "mesh_space_broken_tmp");

        public Pipeline(ResourceLocation name, int priority) {
            super(name);
            this.priority = priority;
        }

        @Override
        public void start() {
            if (started) {
                if (isActive()) {
                    //ClientCommands.Debug();
                    //bufferTarget.copyDepthFrom(getSource());
                    bufferTarget.bindWrite(false);
                }
            } else {
                if (bufferTarget == null) {
                    bufferTarget = TargetManager.getTarget(name);
                    bufferTarget.clear(ON_OSX);
                }

                RenderTarget main = getSource();
                if (isActive()) {
                    //System.out.println("push")
                    bufferTarget.copyDepthFrom(main);
                    PostEffectQueue.add(this);
                    bufferTarget.bindWrite(false);
                    started = true;
                }
                //System.out.println("push");
            }
        }

        @Override
        public void suspend() {
            if (isActive()) {
                //System.out.println("aaaaa");
                bufferTarget.unbindWrite();
                bufferTarget.unbindRead();
                RenderTarget rt = getSource();
                rt.bindWrite(false);
            } else {
                //bufferTarget.clear(Minecraft.ON_OSX);
                getSource().bindWrite(false);
            }
        }

        void handlePasses(RenderTarget src) {
            RenderTarget tmp = TargetManager.getTarget(tmpTarget);
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
            //doDepthCull(src, depth);
            FTPostPasses.space_broken.process(main, src, tmp);
            FTPostPasses.blit.process(tmp, main);
            TargetManager.ReleaseTarget(tmpTarget);
        }

        @Override
        public void PostEffectHandler() {
            //RenderTarget target = TargetManager.getTarget(space_broken_mask);
            handlePasses(bufferTarget);
        }
    }


}
