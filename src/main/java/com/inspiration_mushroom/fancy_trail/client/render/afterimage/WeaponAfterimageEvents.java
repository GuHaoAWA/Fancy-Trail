package com.inspiration_mushroom.fancy_trail.client.render.afterimage;

import com.inspiration_mushroom.fancy_trail.FT;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = FT.MODID, value = Dist.CLIENT)
public class WeaponAfterimageEvents {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        WeaponAfterimageManager.getInstance().update(mc.level.getGameTime());

        WeaponAfterimageRenderer.renderAllGhosts(
                event.getPoseStack(),
                mc.renderBuffers().bufferSource(),
                event.getCamera(),
                // 1.21 NeoForge: RenderLevelStageEvent hands out a DeltaTracker, not a float
                event.getPartialTick().getGameTimeDeltaPartialTick(false)
        );
    }
}
