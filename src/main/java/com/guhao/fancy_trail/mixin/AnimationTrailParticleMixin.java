package com.guhao.fancy_trail.mixin;

import com.guhao.fancy_trail.FTClientConfig;
import com.guhao.fancy_trail.client.render.afterimage.WeaponAfterimageManager;
import com.guhao.fancy_trail.register.ClientModBusEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.SimpleParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = AnimationTrailParticle.Provider.class, remap = false)
public class AnimationTrailParticleMixin {

    @Inject(method = "createParticle*", at = @At("RETURN"))
    public void createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z,
                               double xSpeed, double ySpeed, double zSpeed,
                               CallbackInfoReturnable<Particle> cir) {
        if (cir.getReturnValue() == null) return;

        int eid = (int) Double.doubleToRawLongBits(x);
        int animid = (int) Double.doubleToRawLongBits(z);
        int jointId = (int) Double.doubleToRawLongBits(xSpeed);
        int idx = (int) Double.doubleToRawLongBits(ySpeed);

        // 空气拖尾
        if (FTClientConfig.getAirIsOpen()) {
            level.addParticle(ClientModBusEvent.STATIC_AIR_TRAIL.get(),
                    Double.longBitsToDouble(eid),
                    0,
                    Double.longBitsToDouble(animid),
                    Double.longBitsToDouble(jointId),
                    Double.longBitsToDouble(idx),
                    0);
        }

    }
}
