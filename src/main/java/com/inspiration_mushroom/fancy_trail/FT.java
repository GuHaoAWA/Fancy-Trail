package com.inspiration_mushroom.fancy_trail;


import com.inspiration_mushroom.fancy_trail.client.render.afterimage.WeaponAfterimageManager;
import com.inspiration_mushroom.fancy_trail.register.ClientModBusEvent;
import com.inspiration_mushroom.fancy_trail.register.FTPostPasses;
import com.inspiration_mushroom.fancy_trail.unit.ClientParticleDelayerUnit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(FT.MODID)
public class FT {
    public static final String MODID = "fancy_trail";
    public static final Logger LOGGER = LogManager.getLogger(FT.MODID);


    public FT(IEventBus modEventBus, ModContainer modContainer) {
        FTClientConfig.init(modContainer);
        ClientModBusEvent.PARTICLES.register(modEventBus);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(FTPostPasses::register);
            // Port note: upstream registered `this` on the forge bus for two STATIC handlers
            // (Forge's bus tolerated that); NeoForge's bus wants statics registered via the
            // class. Both events are client-only types, so registration is dist-gated (the
            // handlers below never run on a dedicated server — same effective behavior as
            // 1.20.1, where they simply never fired there).
            NeoForge.EVENT_BUS.register(FT.class);
        }
    }

    // 1.21 NeoForge: TickEvent.RenderTickEvent(Phase.END) -> RenderFrameEvent.Post.
    @SubscribeEvent
    public static void onRenderTick(RenderFrameEvent.Post event) {
        ClientParticleDelayerUnit.onRenderTick(event);
    }

    @SubscribeEvent
    public static void onClientLevelUnload(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientParticleDelayerUnit.clearAll();
        WeaponAfterimageManager.getInstance().onWorldUnload();
    }
}
