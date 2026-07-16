package com.guhao.fancy_trail.mixin;

import com.guhao.fancy_trail.client.render.afterimage.WeaponAfterimageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * 注入 LivingEntityPatch.getModelMatrix() 方法，
 * 在渲染武器残影时返回历史位置的 modelMatrix 而非当前值。
 * <p>
 * 由 WeaponAfterimageManager 的 ThreadLocal 控制：
 * 设置 RENDER_OVERRIDE_MATRIX 后，所有对 getModelMatrix() 的调用
 * 都会返回该覆盖值，直到调用 clearRenderOverride()。
 */
@Mixin(value = LivingEntityPatch.class, remap = false)
public class LivingEntityPatchModelMatrixMixin {

    @Inject(method = "getModelMatrix", at = @At("HEAD"), cancellable = true)
    private void fancy_trail$overrideModelMatrix(float partialTick, CallbackInfoReturnable<OpenMatrix4f> cir) {
        OpenMatrix4f override = WeaponAfterimageManager.RENDER_OVERRIDE_MATRIX.get();
        if (override != null) {
            cir.setReturnValue(override);
        }
    }
}
