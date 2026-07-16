package com.guhao.fancy_trail.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.client.particle.AbstractTrailParticle;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

/**
 * 暴露 AbstractTrailParticle 中 protected 字段的 Accessor Mixin。
 * owner 和 trailInfo 在 AbstractTrailParticle（父类）中声明。
 */
@Mixin(value = AbstractTrailParticle.class, remap = false)
public interface AbstractTrailParticleAccessor {

    @Accessor("owner")
    EntityPatch<?> getOwner();

    @Accessor("trailInfo")
    TrailInfo getTrailInfo();
}
