package com.guhao.fancy_trail.mixin.compat.oculus;

import com.bawnorton.mixinsquared.TargetHandler;
import com.google.common.collect.Sets;
import com.guhao.fancy_trail.client.pipeline.PostParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Queue;
import java.util.Set;


/***
 * For Oculus Compat
 */
@SuppressWarnings("InvalidMemberReference")
@Mixin(value = ParticleEngine.class, priority = 1500)
public abstract class MixinParticleEngineB {

    @TargetHandler(
            mixin = "net.irisshaders.iris.mixin.fantastic.MixinParticleEngine",
            name = "iris$selectParticlesToRender",
            prefix = "redirect"
    )
    @Inject(
            method = "@MixinSquared:Handler",
            at = {@At("RETURN")},
            cancellable = true,
            remap = false
    )
    private void ft$selectParticlesToRender(Map<ParticleRenderType, Queue<Particle>> instance, CallbackInfoReturnable<Set<ParticleRenderType>> cir) {
        Set<ParticleRenderType> keySet = cir.getReturnValue();
        var ret = Sets.filter(keySet, (type) -> {
            return !(type instanceof PostParticleRenderType);
        });

        cir.setReturnValue(ret);
        cir.cancel();
    }
}
