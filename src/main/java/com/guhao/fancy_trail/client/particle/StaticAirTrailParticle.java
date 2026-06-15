package com.guhao.fancy_trail.client.particle;


import com.guhao.fancy_trail.client.render.FTRenderType;
import com.guhao.vix.client.pipeline.PostEffectPipelines;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
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
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;
import java.util.Optional;


@OnlyIn(Dist.CLIENT)
public class StaticAirTrailParticle extends AnimationTrailParticle {

    protected StaticAirTrailParticle(ClientLevel level, LivingEntityPatch<?> owner, Joint joint, AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo) {
        super(level, owner, joint, animation, trailInfo);
    }
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        super.render(pBuffer, pRenderInfo, pPartialTicks);
        if (!PostEffectPipelines.isActive()) return;
        FTRenderType.staticAirDisturbanceRenderType(trailInfo.texturePath()).callPipeline();
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return FTRenderType.staticAirDisturbanceRenderType(trailInfo.texturePath());
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet spriteSet) {}

        @Override
        public Particle createParticle(@NotNull SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            try {
                int eid = (int) Double.doubleToRawLongBits(x);
                int animid = (int) Double.doubleToRawLongBits(z);
                int jointId = (int) Double.doubleToRawLongBits(xSpeed);
                int idx = (int) Double.doubleToRawLongBits(ySpeed);

                Entity entity = level.getEntity(eid);
                if (entity == null) return null;

                if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.distanceToSqr(entity) > 4096.0D) {
                    return null;
                }

                LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
                if (entitypatch == null) return null;

                AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = AnimationManager.byId(animid);
                if (animation == null) return null;

                Optional<List<TrailInfo>> trailInfo = animation.get().getProperty(ClientAnimationProperties.TRAIL_EFFECT);
                if (trailInfo.isEmpty() || idx >= trailInfo.get().size()) return null;

                TrailInfo result = trailInfo.get().get(idx);

                if (result.hand() != null) {
                    ItemStack stack = entitypatch.getOriginal().getItemInHand(result.hand());
                    RenderItemBase renderItemBase = ClientEngine.getInstance().renderEngine.getItemRenderer(stack);
                    if (renderItemBase != null && renderItemBase.trailInfo() != null) {
                        result = renderItemBase.trailInfo().overwrite(result);
                    }
                }

                result = entitypatch.getEntityDecorations().getModifiedTrailInfo(result, result.hand() == null ? CapabilityItem.EMPTY : entitypatch.getAdvancedHoldingItemCapability(result.hand()));

                if (result.playable()) {
                    Joint joint = entitypatch.getArmature().searchJointById(jointId);
                    if (joint == null) return null;
                    return new StaticAirTrailParticle(level, entitypatch, joint, animation, result);
                }
            } catch (Exception ignored) {
            }
            return null;
        }
    }
}