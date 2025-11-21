package com.guhao.fancy_trail.client.render.custom;



import com.guhao.fancy_trail.FT;
import com.guhao.fancy_trail.client.pipeline.PostEffectPipelines;
import com.guhao.fancy_trail.client.pipeline.PostParticleRenderType;
import com.guhao.fancy_trail.client.targets.ScaledTarget;
import com.guhao.fancy_trail.register.FTPostPasses;
import com.guhao.fancy_trail.unit.OjangUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import static net.minecraft.client.Minecraft.ON_OSX;

public class BloomParticleRenderType extends PostParticleRenderType {
    static final PostEffectPipelines.Pipeline ppl = new Pipeline(OjangUtils.newRL(FT.MODID, "bloom_particle"));

    public BloomParticleRenderType(ResourceLocation renderTypeID, ResourceLocation tex) {
        super(renderTypeID, tex);
    }

    private static int NumMul(int a, float b) {
        return (int) (a * Math.max(Math.min(b, 1.5f), 0.8f));
    }

    @Override
    public PostEffectPipelines.Pipeline getPipeline() {
        return ppl;
    }

    public static class Pipeline extends PostEffectPipelines.Pipeline {
        RenderTarget[] blur;
        RenderTarget[] blur_;

        //private static ResourceLocation bloom_particle_target = OjangUtils.newRL(FT.MODID, "bloom_particle_target");
        //private static ResourceLocation bloom_particle_blur = OjangUtils.newRL(FT.MODID, "bloom_particle_blur");
        //private static ResourceLocation bloom_particle_temp = OjangUtils.newRL(FT.MODID, "bloom_particle_temp");
        RenderTarget temp;
        public Pipeline(ResourceLocation name) {
            super(name);
        }

        void handlePasses(RenderTarget src) {
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL12.GL_LINEAR);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL12.GL_LINEAR);
//模糊方式一

            FTPostPasses.downSampler.process(src, blur[0]);    //2
            FTPostPasses.downSampler.process(blur[0], blur[1]);//4
            FTPostPasses.downSampler.process(blur[1], blur[2]);//8
            FTPostPasses.downSampler.process(blur[2], blur[3]);//16
            FTPostPasses.downSampler.process(blur[3], blur[4]);//32
            FTPostPasses.upSampler.process(blur[4], blur_[3], blur[3]);  // 32 -> 16_
            FTPostPasses.upSampler.process(blur_[3], blur_[2], blur[2]);   // 16 -> 8_
            FTPostPasses.upSampler.process(blur_[2], blur_[1], blur[1]);  // 8_ -> 4_
            FTPostPasses.upSampler.process(blur_[1], blur_[0], blur[0]);  // 4_ -> 2_

            FTPostPasses.unity_composite.process(blur_[0], temp, src, Minecraft.getInstance().getMainRenderTarget());

            FTPostPasses.blit.process(temp, Minecraft.getInstance().getMainRenderTarget());

//模糊方式二

//                FTPostPasses.blur.process(src, blur[0], 1, 0 ,3);     // src -> 2
//                FTPostPasses.blur.process(blur[0], blur_[0], 0,1 ,3); // 2 -> 2_
//                FTPostPasses.blur.process(blur_[0], blur[1], 1, 0 ,5); // 2_ -> 4
//                FTPostPasses.blur.process(blur[1], blur_[1], 1, 0 ,5);     // 4 -> 4_
//                FTPostPasses.blur.process(blur_[1], blur[2], 1, 0 ,7); // 4_ -> 8
//                FTPostPasses.blur.process(blur[2], blur_[2], 1, 0 ,7);     // 8 -> 8_
//                FTPostPasses.blur.process(blur_[2], blur[3], 1, 0 ,9); // 8_ -> 16
//                FTPostPasses.blur.process(blur[3], blur_[3], 1, 0 ,9);     // 16 -> 16_
//
//                FTPostPasses.ue_composite.process(src, temp, blur_[0], blur_[1], blur_[2], blur_[3]);
//
//                FTPostPasses.blit.process(temp, Minecraft.getInstance().getMainRenderTarget());


        }

        void initTargets() {
            int cnt = 5;

            if (blur == null) {
                blur = new RenderTarget[cnt];
                float s = 1.f;
                for (int i = 0; i < blur.length; i++) {
                    s /= 2;
                    blur[i] = new ScaledTarget(s, s, bufferTarget.width, bufferTarget.height, false, ON_OSX);
                    blur[i].setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                    blur[i].clear(ON_OSX);
                    if (bufferTarget.isStencilEnabled()) blur[i].enableStencil();
                }
            }

            if (blur_ == null) {
                blur_ = new RenderTarget[cnt - 1];
                float s = 1.f;
                for (int i = 0; i < blur_.length; i++) {
                    s /= 2;
                    blur_[i] = new ScaledTarget(s, s, bufferTarget.width, bufferTarget.height, false, ON_OSX);
                    blur_[i].setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                    blur_[i].clear(ON_OSX);
                    if (bufferTarget.isStencilEnabled()) blur[i].enableStencil();
                }
            }

            if (temp == null) {
                temp = createTempTarget(bufferTarget);
            }

            if (temp.width != bufferTarget.width || temp.height != bufferTarget.height) {
                for (int i = 0; i < blur.length; i++) {
                    blur[i].resize(bufferTarget.width, bufferTarget.height, ON_OSX);
                }

                for (int i = 0; i < blur_.length; i++) {
                    blur_[i].resize(bufferTarget.width, bufferTarget.height, ON_OSX);
                }
                temp.resize(bufferTarget.width, bufferTarget.height, ON_OSX);
            }
        }

        @Override
        public void PostEffectHandler() {
            initTargets();
            handlePasses(bufferTarget);
            //oldHandel(target);
        }
    }

}
