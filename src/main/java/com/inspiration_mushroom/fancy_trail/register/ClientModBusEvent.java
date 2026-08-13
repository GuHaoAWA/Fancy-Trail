package com.inspiration_mushroom.fancy_trail.register;


import com.inspiration_mushroom.fancy_trail.FT;
import com.inspiration_mushroom.fancy_trail.client.particle.*;
import com.inspiration_mushroom.fancy_trail.client.particle.flow.*;
import com.inspiration_mushroom.fancy_trail.client.render.afterimage.WeaponAfterImageTriggerParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 21.1: EventBusSubscriber auto-detects the bus from the handler event types; the Bus attribute is deprecated-for-removal.
@EventBusSubscriber(modid = FT.MODID, value = Dist.CLIENT)
public class ClientModBusEvent {
    // 1.21 NeoForge: ForgeRegistries.PARTICLE_TYPES/RegistryObject -> BuiltInRegistries.PARTICLE_TYPE/DeferredHolder.
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, FT.MODID);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOM_TRAIL = PARTICLES.register("bloom_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLOWING_BLOOM_TRAIL = PARTICLES.register("flowing_bloom_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPACE_TRAIL = PARTICLES.register("space_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLOWING_SPACE_TRAIL = PARTICLES.register("flowing_space_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AIR_TRAIL = PARTICLES.register("air_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STATIC_AIR_TRAIL = PARTICLES.register("static_air_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLOWING_STATIC_AIR_TRAIL = PARTICLES.register("flowing_static_air_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLOWING_AIR_TRAIL = PARTICLES.register("flowing_air_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CHROMATIC_TRAIL = PARTICLES.register("chromatic_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLOWING_CHROMATIC_TRAIL = PARTICLES.register("flowing_chromatic_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RGB_TRAIL = PARTICLES.register("rgb_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLOWING_RGB_TRAIL = PARTICLES.register("flowing_rgb_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STARRY_TRAIL = PARTICLES.register("starry_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLOWING_STARRY_TRAIL = PARTICLES.register("flowing_starry_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AFTER_IMAGE_TRAIL = PARTICLES.register("after_image_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLOWING_AFTER_IMAGE_TRAIL = PARTICLES.register("flowing_after_image_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ENDER_TRAIL = PARTICLES.register("ender_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AAA_TRAIL = PARTICLES.register("aaa_trail", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE_TRAIL = PARTICLES.register("fire_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BURST_TRAIL = PARTICLES.register("burst_trail", () -> new SimpleParticleType(true));

    // renders nothing; its presence in a trail_effects entry is the switch that arms the afterimage capture
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WEAPON_AFTERIMAGE = PARTICLES.register("weapon_afterimage", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLOWING_ANIMATION_TRAIL = PARTICLES.register("flowing_trail", () -> new SimpleParticleType(true));
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onParticleRegistry(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(BLOOM_TRAIL.get(), BloomTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_BLOOM_TRAIL.get(), FlowingBloomTrailParticle.Provider::new);
        event.registerSpriteSet(SPACE_TRAIL.get(), SpaceTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_SPACE_TRAIL.get(), FlowingSpaceTrailParticle.Provider::new);
        event.registerSpriteSet(AIR_TRAIL.get(), AirTrailParticle.Provider::new);
        event.registerSpriteSet(STATIC_AIR_TRAIL.get(), StaticAirTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_AIR_TRAIL.get(), FlowingAirTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_STATIC_AIR_TRAIL.get(), FlowingStaticAirTrailParticle.Provider::new);
        event.registerSpriteSet(CHROMATIC_TRAIL.get(), ChromaticTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_CHROMATIC_TRAIL.get(), FlowingChromaticTrailParticle.Provider::new);
        event.registerSpriteSet(RGB_TRAIL.get(), RGBTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_RGB_TRAIL.get(), FlowingRGBTrailParticle.Provider::new);
        event.registerSpriteSet(STARRY_TRAIL.get(), StarryTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_STARRY_TRAIL.get(), FlowingStarryTrailParticle.Provider::new);
        event.registerSpriteSet(AFTER_IMAGE_TRAIL.get(), AfterImageTrailParticle.Provider::new);
        event.registerSpriteSet(FLOWING_AFTER_IMAGE_TRAIL.get(), FlowingAfterImageTrailParticle.Provider::new);
        event.registerSpriteSet(ENDER_TRAIL.get(), EnderTrailParticle.Provider::new);
        event.registerSpriteSet(AAA_TRAIL.get(), AAATrailParticle.Provider::new);
        event.registerSpriteSet(FIRE_TRAIL.get(), FireTrailParticle.Provider::new);
        event.registerSpriteSet(BURST_TRAIL.get(), BrustTrailParticle.Provider::new);
        event.registerSpriteSet(WEAPON_AFTERIMAGE.get(), WeaponAfterImageTriggerParticle.Provider::new);
        event.registerSpriteSet(FLOWING_ANIMATION_TRAIL.get(), FlowingAnimationTrailParticle.Provider::new);

    }



}
