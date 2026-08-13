package com.inspiration_mushroom.fancy_trail.mixin;

import com.inspiration_mushroom.fancy_trail.FT;
import com.inspiration_mushroom.fancy_trail.FTClientConfig;
import com.inspiration_mushroom.fancy_trail.client.render.afterimage.WeaponAfterimageManager;
import com.inspiration_mushroom.fancy_trail.register.ClientModBusEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = AnimationTrailParticle.class, remap = false)
public class WeaponAfterimageCaptureMixin {

    @Unique
    private static boolean fancy_trail$loggedOnce = false;

    @Inject(method = "createNextCurve", at = @At("RETURN"))
    private void fancy_trail$captureWeaponTransform(CallbackInfo ci) {
        if (!FTClientConfig.getWeaponAfterimageEnabled()) return;

        AbstractTrailParticleAccessor baseAccessor = (AbstractTrailParticleAccessor) this;
        AnimationTrailParticleFieldAccessor atpAccessor = (AnimationTrailParticleFieldAccessor) this;

        TrailInfo trailInfo = baseAccessor.getTrailInfo();
        if (trailInfo == null) return;
        if (!trailInfo.particle().equals(ClientModBusEvent.WEAPON_AFTERIMAGE.get())) return;

        InteractionHand hand = trailInfo.hand();
        if (hand == null) return;

        EntityPatch<?> ownerRaw = baseAccessor.getOwner();
        if (!(ownerRaw instanceof LivingEntityPatch<?> entityPatch)) return;

        LivingEntity entity = entityPatch.getOriginal();
        if (entity == null || !entity.isAlive()) return;

        Joint joint = atpAccessor.getJoint();
        if (joint == null) return;

        if (!fancy_trail$loggedOnce) {
            FT.LOGGER.info("weapon afterimage: capturing for {} joint={} anim={}",
                    entity.getName().getString(), joint.getName(), atpAccessor.getAnimation());
            fancy_trail$loggedOnce = true;
        }

        Pose currentPose = entityPatch.getAnimator().getPose(1.0F);
        OpenMatrix4f jointLocalTf = entityPatch.getArmature().getBoundTransformFor(currentPose, joint);
        Vec3 posCur = entity.getPosition(1.0F);

        ItemStack weaponStack = entity.getItemInHand(hand);
        if (weaponStack.isEmpty()) return;

        WeaponAfterimageManager.getInstance().captureSnapshot(
                entityPatch, jointLocalTf, currentPose,
                posCur, entity.getYRot(), weaponStack,
                hand, joint.getId(), entity.level().getGameTime()
        );
    }
}
