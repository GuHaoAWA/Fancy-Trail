package com.inspiration_mushroom.fancy_trail.client.render.afterimage;

import com.inspiration_mushroom.fancy_trail.FT;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 武器残影渲染事件监听器。
 * 使用 @EventBusSubscriber 自动注册到 FORGE 事件总线，比手动 register(this) 更可靠。
 */
@Mod.EventBusSubscriber(modid = FT.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WeaponAfterimageEvents {

    private static boolean loggedOnce = false;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!loggedOnce) {
            FT.LOGGER.info("[WeaponAfterimage] RenderLevelStageEvent fired! stage={}", event.getStage());
            loggedOnce = true;
        }

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        WeaponAfterimageManager.getInstance().update(mc.level.getGameTime());

        WeaponAfterimageRenderer.renderAllGhosts(
                event.getPoseStack(),
                mc.renderBuffers().bufferSource(),
                event.getCamera(),
                event.getPartialTick()
        );
    }
}
