package com.guhao.fancy_trail.mixin;

import com.guhao.fancy_trail.FT;
import com.guhao.fancy_trail.FTClientConfig;
import com.guhao.fancy_trail.client.render.afterimage.WeaponAfterimageManager;
import com.guhao.fancy_trail.register.ClientModBusEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
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

/**
 * 注入 AnimationTrailParticle.createNextCurve() 的 RETURN 点。
 * 只在 trailInfo.particle 匹配 WEAPON_AFTERIMAGE 时捕获武器变换。
 * 首次捕获时自动注册到 Manager，不依赖 AnimationTrailParticleMixin。
 */
@Mixin(value = AnimationTrailParticle.class, remap = false)
public class WeaponAfterimageCaptureMixin {

    private static boolean fancy_trail$loggedOnce = false;

    @Inject(method = "createNextCurve", at = @At("RETURN"))
    private void fancy_trail$captureWeaponTransform(CallbackInfo ci) {
        if (!FTClientConfig.getWeaponAfterimageEnabled()) return;

        AbstractTrailParticleAccessor baseAccessor = (AbstractTrailParticleAccessor) this;
        AnimationTrailParticleFieldAccessor atpAccessor = (AnimationTrailParticleFieldAccessor) this;

        TrailInfo trailInfo = baseAccessor.getTrailInfo();
        if (trailInfo == null) return;

        // 只捕获 WEAPON_AFTERIMAGE 触发器粒子
        if (!trailInfo.particle().equals(ClientModBusEvent.WEAPON_AFTERIMAGE.get())) return;

        // 必须是武器拖尾
        InteractionHand hand = trailInfo.hand();
        if (hand == null) return;

        EntityPatch<?> ownerRaw = baseAccessor.getOwner();
        if (!(ownerRaw instanceof LivingEntityPatch<?> entityPatch)) return;

        LivingEntity entity = (LivingEntity) entityPatch.getOriginal();
        if (entity == null || !entity.isAlive()) return;

        Joint joint = atpAccessor.getJoint();
        if (joint == null) return;

        // 首次捕获时打印日志确认触发
        if (!fancy_trail$loggedOnce) {
            FT.LOGGER.info("[WeaponAfterimage] Capturing weapon transforms for entity: {} joint: {} anim: {}",
                    entity.getName().getString(), joint.getName(),
                    atpAccessor.getAnimation().toString());
            fancy_trail$loggedOnce = true;
        }

        Vec3 posCur = entity.getPosition(1.0F);
        Pose currentPose = entityPatch.getAnimator().getPose(1.0F);
        OpenMatrix4f jointLocalTf = entityPatch.getArmature().getBoundTransformFor(currentPose, joint);

        ItemStack weaponStack = entity.getItemInHand(hand);
        if (weaponStack == null || weaponStack.isEmpty()) return;

        WeaponAfterimageManager.getInstance().captureSnapshot(
                entityPatch, jointLocalTf, currentPose,
                posCur, entity.getYRot(), weaponStack,
                hand, joint.getId(), entity.level().getGameTime()
        );
    }
}
