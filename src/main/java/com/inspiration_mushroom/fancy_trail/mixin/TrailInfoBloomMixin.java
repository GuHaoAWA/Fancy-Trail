package com.inspiration_mushroom.fancy_trail.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.inspiration_mushroom.fancy_trail.client.render.custom.BloomSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.animation.property.TrailInfo;

@Mixin(value = TrailInfo.class, remap = false)
public class TrailInfoBloomMixin {
    @Inject(
            method = "deserialize(Lcom/google/gson/JsonElement;)Lyesman/epicfight/api/client/animation/property/TrailInfo;",
            at = @At("RETURN")
    )
    private static void fancy_trail$parseBloom(JsonElement json, CallbackInfoReturnable<TrailInfo> cir) {
        try {
            if (json == null || !json.isJsonObject() || cir.getReturnValue() == null) {
                return;
            }

            JsonObject trail = json.getAsJsonObject();
            if (!trail.has("bloom") || !trail.get("bloom").isJsonObject()) {
                return;
            }

            BloomSettings.register(cir.getReturnValue(), BloomSettings.fromJson(trail.getAsJsonObject("bloom")));
        } catch (RuntimeException ignored) {
        }
    }
}
