package com.guhao.fancy_trail.mixin.compat.oculus;


import com.google.common.collect.Sets;
import com.guhao.fancy_trail.client.pipeline.PostParticleRenderType;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngineA {

    @ModifyReceiver(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;keySet()Ljava/util/Set;"
            ),
            remap = false
    )
    private Map<ParticleRenderType, Queue<Particle>> ft$modifyParticleMap(Map<ParticleRenderType, Queue<Particle>> originalMap) {
        return new java.util.AbstractMap<>() {
            @Override
            public @NotNull Set<Entry<ParticleRenderType, Queue<Particle>>> entrySet() {
                return originalMap.entrySet();
            }

            @Override
            public @NotNull Set<ParticleRenderType> keySet() {

                return Sets.filter(originalMap.keySet(),
                        (type) -> !(type instanceof PostParticleRenderType));
            }

            @Override
            public Queue<Particle> get(Object key) {
                return originalMap.get(key);
            }
        };
    }
}