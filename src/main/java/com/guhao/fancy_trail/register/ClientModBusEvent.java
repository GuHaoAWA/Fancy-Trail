package com.guhao.fancy_trail.register;


import com.guhao.fancy_trail.FT;
import com.guhao.fancy_trail.client.particle.*;
import com.guhao.fancy_trail.client.particle.flow.*;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = FT.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModBusEvent {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, FT.MODID);
    public static final RegistryObject<SimpleParticleType> BLOOM_TRAIL = PARTICLES.register("bloom_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLOWING_BLOOM_TRAIL = PARTICLES.register("flowing_bloom_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SPACE_TRAIL = PARTICLES.register("space_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLOWING_SPACE_TRAIL = PARTICLES.register("flowing_space_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> AIR_TRAIL = PARTICLES.register("air_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLOWING_AIR_TRAIL = PARTICLES.register("flowing_air_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> CHROMATIC_TRAIL = PARTICLES.register("chromatic_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLOWING_CHROMATIC_TRAIL = PARTICLES.register("flowing_chromatic_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> RGB_TRAIL = PARTICLES.register("rgb_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLOWING_RGB_TRAIL = PARTICLES.register("flowing_rgb_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> STARRY_TRAIL = PARTICLES.register("starry_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLOWING_STARRY_TRAIL = PARTICLES.register("flowing_starry_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ENDER_TRAIL = PARTICLES.register("ender_trail", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> FIRE_TRAIL = PARTICLES.register("fire_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BURST_TRAIL = PARTICLES.register("burst_trail", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> FLOWING_ANIMATION_TRAIL = PARTICLES.register("flowing_trail", () -> new SimpleParticleType(true));
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onParticleRegistry(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(BLOOM_TRAIL.get(), BloomTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_BLOOM_TRAIL.get(), FlowingBloomTrailParticle.Provider::new);
        event.registerSpriteSet(SPACE_TRAIL.get(), SpaceTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_SPACE_TRAIL.get(), FlowingSpaceTrailParticle.Provider::new);
        event.registerSpriteSet(AIR_TRAIL.get(), AirTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_AIR_TRAIL.get(), FlowingAirTrailParticle.Provider::new);
        event.registerSpriteSet(CHROMATIC_TRAIL.get(), ChromaticTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_CHROMATIC_TRAIL.get(), FlowingChromaticTrailParticle.Provider::new);
        event.registerSpriteSet(RGB_TRAIL.get(), RGBTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_RGB_TRAIL.get(), FlowingRGBTrailParticle.Provider::new);
        event.registerSpriteSet(STARRY_TRAIL.get(), StarryTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_STARRY_TRAIL.get(), FlowingStarryTrailParticle.Provider::new);
        event.registerSpriteSet(ENDER_TRAIL.get(), EnderTrailParticle.Provider::new);
        event.registerSpriteSet(FIRE_TRAIL.get(), FireTrailParticle.Provider::new);
        event.registerSpriteSet(BURST_TRAIL.get(), BrustTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_ANIMATION_TRAIL.get(), FlowingAnimationTrailParticle.Provider::new);

    }



}
