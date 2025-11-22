package com.guhao.fancy_trail;


import com.guhao.fancy_trail.register.ClientModBusEvent;
import com.guhao.fancy_trail.register.FTPostPasses;
import com.guhao.fancy_trail.unit.ClientParticleDelayerUnit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(FT.MODID)
public class FT {
    public static final String MODID = "fancy_trail";
    public static final Logger LOGGER = LogManager.getLogger(FT.MODID);


    public FT(FMLJavaModLoadingContext context) {
        FTClientConfig.init();
        final IEventBus modEventBus = context.getModEventBus();
        final IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        ClientModBusEvent.PARTICLES.register(modEventBus);
        if(FMLEnvironment.dist == Dist.CLIENT){
            modEventBus.addListener(FTPostPasses::register);
        }
        forgeEventBus.register(this);
        }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        ClientParticleDelayerUnit.onRenderTick(event);
    }

    @SubscribeEvent
    public static void onClientLevelUnload(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientParticleDelayerUnit.clearAll();
    }
}


