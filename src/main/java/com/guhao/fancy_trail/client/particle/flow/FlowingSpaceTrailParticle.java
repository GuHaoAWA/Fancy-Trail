package com.guhao.fancy_trail.client.particle.flow;

import com.guhao.fancy_trail.client.pipeline.PostEffectPipelines;
import com.guhao.fancy_trail.client.render.FTRenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.Optional;


@OnlyIn(Dist.CLIENT)
public class FlowingSpaceTrailParticle extends FlowingAnimationTrailParticle {


    protected FlowingSpaceTrailParticle(ClientLevel level, LivingEntityPatch<?> owner, Joint joint, AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo) {
        super(level, owner, joint, animation, trailInfo);
    }

    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        if (!PostEffectPipelines.isActive()) return;
        FTRenderType.spaceTrailRenderType(trailInfo.texturePath()).callPipeline();
        super.render(pBuffer, pRenderInfo, pPartialTicks);
    }



    public @NotNull ParticleRenderType getRenderType() {
        return FTRenderType.spaceTrailRenderType(trailInfo.texturePath());
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
                return new FlowingSpaceTrailParticle(level, entitypatch, entitypatch.getArmature().searchJointById(jointId), animation, result);
            } else {
                return null;
            }
        }
    }
}
