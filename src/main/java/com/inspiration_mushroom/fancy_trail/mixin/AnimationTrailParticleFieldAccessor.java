package com.inspiration_mushroom.fancy_trail.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.client.particle.AnimationTrailParticle;

@Mixin(value = AnimationTrailParticle.class, remap = false)
public interface AnimationTrailParticleFieldAccessor {

    @Accessor("joint")
    Joint getJoint();

    @Accessor("animation")
    AssetAccessor<? extends StaticAnimation> getAnimation();
}
