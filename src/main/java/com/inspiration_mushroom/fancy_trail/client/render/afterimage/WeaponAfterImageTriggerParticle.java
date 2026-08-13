package com.inspiration_mushroom.fancy_trail.client.render.afterimage;

import com.inspiration_mushroom.fancy_trail.FT;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;
import java.util.Optional;

// Draws nothing. Naming it as an animation's "particle_type": "fancy_trail:weapon_afterimage"
// is what arms WeaponAfterimageCaptureMixin for that trail.
@OnlyIn(Dist.CLIENT)
public class WeaponAfterImageTriggerParticle extends AnimationTrailParticle {

    protected WeaponAfterImageTriggerParticle(
            ClientLevel level,
            LivingEntityPatch<?> owner,
            Joint joint,
            AssetAccessor<? extends StaticAnimation> animation,
            TrailInfo trailInfo) {
        super(level, owner, joint, animation, trailInfo);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.NO_RENDER;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private static boolean loggedOnce = false;

        public Provider(SpriteSet spriteSet) {
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            int eid = (int) Double.doubleToRawLongBits(x);
            int animid = (int) Double.doubleToRawLongBits(z);
            int jointId = (int) Double.doubleToRawLongBits(xSpeed);
            int idx = (int) Double.doubleToRawLongBits(ySpeed);

            Entity entity = level.getEntity(eid);
            if (entity == null) {
                if (!loggedOnce) FT.LOGGER.warn("weapon afterimage: no entity for eid={}", eid);
                return null;
            }

            LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
            if (entitypatch == null) {
                if (!loggedOnce) FT.LOGGER.warn("weapon afterimage: no entity patch for {}", entity.getName().getString());
                return null;
            }

            AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = AnimationManager.byId(animid);
            if (animation == null) {
                if (!loggedOnce) FT.LOGGER.warn("weapon afterimage: no animation for animid={}", animid);
                return null;
            }

            Optional<List<TrailInfo>> trailInfoOpt = animation.get().getProperty(ClientAnimationProperties.TRAIL_EFFECT);
            if (trailInfoOpt.isEmpty()) {
                if (!loggedOnce) FT.LOGGER.warn("weapon afterimage: no trail effects on {}", animation);
                return null;
            }

            TrailInfo result = trailInfoOpt.get().get(idx);

            if (result.hand() != null) {
                ItemStack stack = entitypatch.getOriginal().getItemInHand(result.hand());
                RenderItemBase renderItemBase = RenderEngine.getInstance().getItemRenderer(stack);
                if (renderItemBase != null && renderItemBase.trailInfo() != null) {
                    result = renderItemBase.trailInfo().overwrite(result);
                }
            }
            result = entitypatch.getEntityDecorations().getModifiedTrailInfo(
                    result,
                    result.hand() == null ? CapabilityItem.EMPTY : entitypatch.getAdvancedHoldingItemCapability(result.hand())
            );

            if (!result.playable()) {
                if (!loggedOnce) {
                    FT.LOGGER.warn("weapon afterimage: trail not playable, particle={} start={} end={}",
                            result.particle(), result.startTime(), result.endTime());
                }
                loggedOnce = true;
                return null;
            }

            loggedOnce = true;

            return new WeaponAfterImageTriggerParticle(
                    level, entitypatch,
                    entitypatch.getArmature().searchJointById(jointId),
                    animation, result);
        }
    }
}
