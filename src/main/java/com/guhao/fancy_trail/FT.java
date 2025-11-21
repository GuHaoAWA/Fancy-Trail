package com.guhao.fancy_trail;


import com.guhao.fancy_trail.register.ClientModBusEvent;
import com.guhao.fancy_trail.register.FTPostPasses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
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


}


