package com.guhao.fancy_trail.mixin.compat.oculus;

import com.google.common.collect.Sets;
import com.guhao.fancy_trail.client.pipeline.PostParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngineA {

    @ModifyVariable(
            method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V"},
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;keySet()Ljava/util/Set;"
            ),
            name = "keySet",
            remap = false
    )
    private Set<ParticleRenderType> ft$modifyParticlesToRender(Set<ParticleRenderType> original) {
        return Sets.filter(original, (type) -> !(type instanceof PostParticleRenderType));
    }
}