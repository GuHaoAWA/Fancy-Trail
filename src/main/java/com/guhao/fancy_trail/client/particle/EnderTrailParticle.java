    package com.guhao.fancy_trail.client.particle;


    import com.mojang.blaze3d.vertex.PoseStack;
    import com.mojang.blaze3d.vertex.VertexConsumer;
    import net.minecraft.client.Camera;
    import net.minecraft.client.Minecraft;
    import net.minecraft.client.multiplayer.ClientLevel;
    import net.minecraft.client.particle.Particle;
    import net.minecraft.client.particle.ParticleProvider;
    import net.minecraft.client.particle.ParticleRenderType;
    import net.minecraft.client.particle.SpriteSet;
    import net.minecraft.client.renderer.RenderType;
    import net.minecraft.core.particles.SimpleParticleType;
    import net.minecraft.util.Mth;
    import net.minecraft.world.entity.Entity;
    import net.minecraft.world.item.ItemStack;
    import net.minecraftforge.api.distmarker.Dist;
    import net.minecraftforge.api.distmarker.OnlyIn;
    import org.jetbrains.annotations.NotNull;
    import org.joml.Matrix4f;
    import org.joml.Vector4f;
    import yesman.epicfight.api.animation.AnimationManager;
    import yesman.epicfight.api.animation.Joint;
    import yesman.epicfight.api.animation.types.StaticAnimation;
    import yesman.epicfight.api.asset.AssetAccessor;
    import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
    import yesman.epicfight.api.client.animation.property.TrailInfo;
    import yesman.epicfight.client.ClientEngine;
    import yesman.epicfight.client.particle.AnimationTrailParticle;
    import yesman.epicfight.client.particle.EpicFightParticleRenderTypes;
    import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
    import yesman.epicfight.world.capabilities.EpicFightCapabilities;
    import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

    import java.util.List;
    import java.util.Optional;

    /**
     * 待实现
     */
    @OnlyIn(Dist.CLIENT)
    public class EnderTrailParticle extends AnimationTrailParticle {


        protected EnderTrailParticle(ClientLevel level, LivingEntityPatch<?> owner, Joint joint, AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo) {
            super(level, owner, joint, animation, trailInfo);
        }

        @Override
        public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
            if (this.trailEdges.isEmpty()) {
                return;
            }


            PoseStack poseStack = new PoseStack();
            int light = this.getLightColor(partialTick);
            this.setupPoseStack(poseStack, camera, partialTick);
            Matrix4f matrix4f = poseStack.last().pose();
            int edges = this.trailEdges.size() - 1;
            boolean startFade = this.trailEdges.get(0).lifetime == 1;
            boolean endFade = this.trailEdges.get(edges).lifetime == this.trailInfo.trailLifetime();
            float startEdge = (startFade ? this.trailInfo.interpolateCount() * 2 * partialTick : 0.0F) + this.startEdgeCorrection;
            float endEdge = endFade ? Math.min(edges - (this.trailInfo.interpolateCount() * 2) * (1.0F - partialTick), edges - 1) : edges - 1;
            float interval = 1.0F / (endEdge - startEdge);
            float fading = 1.0F;

            if (this.shouldRemove) {
                if (TrailInfo.isValidTime(this.trailInfo.fadeTime())) {
                    fading = ((float)(this.lifetime - this.age) / (float)this.trailInfo.trailLifetime());
                } else {
                    fading = Mth.clamp(((this.lifetime - this.age) + (1.0F - partialTick)) / this.trailInfo.trailLifetime(), 0.0F, 1.0F);
                }
            }

            float partialStartEdge = interval * (startEdge % 1.0F);
            float from = -partialStartEdge;
            float to = -partialStartEdge + interval;
            RenderType renderType = RenderType.endGateway();
            VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(renderType);
            vertexConsumer = buffer;

            for (int i = (int)(startEdge); i < (int)endEdge + 1; i++) {
                TrailEdge e1 = this.trailEdges.get(i);
                TrailEdge e2 = this.trailEdges.get(i + 1);
                Vector4f pos1 = new Vector4f((float)e1.start.x, (float)e1.start.y, (float)e1.start.z, 1.0F);
                Vector4f pos2 = new Vector4f((float)e1.end.x, (float)e1.end.y, (float)e1.end.z, 1.0F);
                Vector4f pos3 = new Vector4f((float)e2.end.x, (float)e2.end.y, (float)e2.end.z, 1.0F);
                Vector4f pos4 = new Vector4f((float)e2.start.x, (float)e2.start.y, (float)e2.start.z, 1.0F);

                pos1.mul(matrix4f);
                pos2.mul(matrix4f);
                pos3.mul(matrix4f);
                pos4.mul(matrix4f);

                float alphaFrom = Mth.clamp(from, 0.0F, 1.0F);
                float alphaTo = Mth.clamp(to, 0.0F, 1.0F);

                vertexConsumer.vertex(pos1.x(), pos1.y(), pos1.z()).uv(from, 1.0F).color(this.rCol, this.gCol, this.bCol, this.alpha * alphaFrom * fading).uv2(light).endVertex();
                vertexConsumer.vertex(pos2.x(), pos2.y(), pos2.z()).uv(from, 0.0F).color(this.rCol, this.gCol, this.bCol, this.alpha * alphaFrom * fading).uv2(light).endVertex();
                vertexConsumer.vertex(pos3.x(), pos3.y(), pos3.z()).uv(to, 0.0F).color(this.rCol, this.gCol, this.bCol, this.alpha * alphaTo * fading).uv2(light).endVertex();
                vertexConsumer.vertex(pos4.x(), pos4.y(), pos4.z()).uv(to, 1.0F).color(this.rCol, this.gCol, this.bCol, this.alpha * alphaTo * fading).uv2(light).endVertex();

                from += interval;
                to += interval;

            }
        }

        public ParticleRenderType getRenderType() {
            return EpicFightParticleRenderTypes.TRAIL_EFFECT.apply(this.trailInfo.texturePath());
        }


        @OnlyIn(Dist.CLIENT)
        public static class Provider implements ParticleProvider<SimpleParticleType> {
            public Provider(SpriteSet spriteSet) {
            }

            @Override
            public Particle createParticle(@NotNull SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                int eid = (int) Double.doubleToRawLongBits(x);
                int animid = (int) Double.doubleToRawLongBits(z);
                int jointId = (int) Double.doubleToRawLongBits(xSpeed);
                int idx = (int) Double.doubleToRawLongBits(ySpeed);
                Entity entity = level.getEntity(eid);

                if (entity == null) {
                    return null;
                }

                LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

                if (entitypatch == null) {
                    return null;
                }

                AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = AnimationManager.byId(animid);

                if (animation == null) {
                    return null;
                }

                Optional<List<TrailInfo>> trailInfo = animation.get().getProperty(ClientAnimationProperties.TRAIL_EFFECT);

                if (trailInfo.isEmpty()) {
                    return null;
                }

                TrailInfo result = trailInfo.get().get(idx);

                if (result.hand() != null) {
                    ItemStack stack = entitypatch.getOriginal().getItemInHand(result.hand());
                    RenderItemBase renderItemBase = ClientEngine.getInstance().renderEngine.getItemRenderer(stack);

                    if (renderItemBase != null && renderItemBase.trailInfo() != null) {
                        result = renderItemBase.trailInfo().overwrite(result);
                    }
                }

                if (result.playable()) {
                    return new EnderTrailParticle(level, entitypatch, entitypatch.getArmature().searchJointById(jointId), animation, result);
                } else {
                    return null;
                }
            }
        }
    }
