package com.inspiration_mushroom.fancy_trail.mixin;

import com.inspiration_mushroom.fancy_trail.client.render.afterimage.WeaponAfterimageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = LivingEntityPatch.class, remap = false)
public class LivingEntityPatchModelMatrixMixin {

    // only non-null while WeaponAfterimageRenderer is drawing a ghost
    @Inject(method = "getModelMatrix", at = @At("HEAD"), cancellable = true)
    private void fancy_trail$overrideModelMatrix(float partialTick, CallbackInfoReturnable<OpenMatrix4f> cir) {
        OpenMatrix4f override = WeaponAfterimageManager.RENDER_OVERRIDE_MATRIX.get();
        if (override != null) {
            cir.setReturnValue(override);
        }
    }
}
