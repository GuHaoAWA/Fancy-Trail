package com.guhao.fancy_trail.client.render.afterimage;

import com.guhao.fancy_trail.FT;
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

/**
 * 武器3D模型残影触发器粒子。
 * <p>
 * 此粒子**不渲染任何视觉效果**——它的唯一作用是作为触发器：
 * 当 Epic Fight 动画数据中指定了 {@code "particle_type": "fancy_trail:weapon_afterimage"}，
 * 此粒子会在武器拖尾期间被创建，从而激活 {@link WeaponAfterimageManager} 的变换捕获
 * 和 {@link WeaponAfterimageRenderer} 的幽灵模型渲染。
 * <p>
 * 用法示例（动画 JSON data 文件）：
 * <pre>
 * {
 *   "trail_effects": [{
 *     "start_time": 0.05,
 *     "end_time": 0.5,
 *     "joint": "Tool_R",
 *     "item_skin_hand": "main_hand",
 *     "particle_type": "fancy_trail:weapon_afterimage"
 *   }]
 * }
 * </pre>
 */
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
        // 不渲染任何视觉效果——此粒子只做触发器
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

            if (!loggedOnce) {
                FT.LOGGER.info("[WeaponAfterimage] Provider.createParticle called! eid={} animid={} jointId={} idx={}",
                        eid, animid, jointId, idx);
            }

            Entity entity = level.getEntity(eid);
            if (entity == null) {
                if (!loggedOnce) FT.LOGGER.warn("[WeaponAfterimage] entity==null for eid={}", eid);
                return null;
            }

            LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
            if (entitypatch == null) {
                if (!loggedOnce) FT.LOGGER.warn("[WeaponAfterimage] entitypatch==null for entity={}", entity.getName().getString());
                return null;
            }

            AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = AnimationManager.byId(animid);
            if (animation == null) {
                if (!loggedOnce) FT.LOGGER.warn("[WeaponAfterimage] animation==null for animid={}", animid);
                return null;
            }

            Optional<List<TrailInfo>> trailInfoOpt = animation.get().getProperty(ClientAnimationProperties.TRAIL_EFFECT);
            if (trailInfoOpt.isEmpty()) {
                if (!loggedOnce) FT.LOGGER.warn("[WeaponAfterimage] trailInfoOpt is empty for anim={}", animation);
                return null;
            }

            TrailInfo result = trailInfoOpt.get().get(idx);

            if (result.hand() != null) {
                ItemStack stack = entitypatch.getOriginal().getItemInHand(result.hand());
                RenderItemBase renderItemBase = ClientEngine.getInstance().renderEngine.getItemRenderer(stack);
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
                    FT.LOGGER.warn("[WeaponAfterimage] trail not playable! particle={} start={} end={}",
                            result.particle(), result.startTime(), result.endTime());
                }
                return null;
            }

            if (!loggedOnce) {
                FT.LOGGER.info("[WeaponAfterimage] Particle created successfully! particle={}", result.particle());
                loggedOnce = true;
            }

            return new WeaponAfterImageTriggerParticle(
                    level, entitypatch,
                    entitypatch.getArmature().searchJointById(jointId),
                    animation, result);
        }
    }
}
