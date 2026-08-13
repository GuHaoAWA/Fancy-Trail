package com.inspiration_mushroom.fancy_trail;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class FTClientConfig {
    public static final ClientConfig CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    static {
        final Pair<ClientConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static class ClientConfig {
        public final ModConfigSpec.DoubleValue starryTrailIntensity;
        public final ModConfigSpec.DoubleValue starryTrailStarScale;
        public final ModConfigSpec.DoubleValue starryTrailOpacity;
        public final ModConfigSpec.IntValue starryTrailLayers;
        public final ModConfigSpec.ConfigValue<String> starryTrailTexture;

        public final ModConfigSpec.DoubleValue chromaticEffect;

        public final ModConfigSpec.DoubleValue additionalParticleSpeed;
        public final ModConfigSpec.ConfigValue<String> additionalParticleType;

        public final ModConfigSpec.DoubleValue flowingIntensity;
        public final ModConfigSpec.DoubleValue flowingSpeed;

        public final ModConfigSpec.BooleanValue isOpenAir;

        public final ModConfigSpec.BooleanValue weaponAfterimageEnabled;
        public final ModConfigSpec.IntValue weaponAfterimageGhostCount;
        public final ModConfigSpec.DoubleValue weaponAfterimageMaxAlpha;
        public final ModConfigSpec.DoubleValue weaponAfterimageFadeExponent;
        public final ModConfigSpec.IntValue weaponAfterimageMaxAgeTicks;

        public ClientConfig(ModConfigSpec.Builder builder) {
            builder.comment("Fancy Trail Client Configuration")
                    .push("starry_trail");

            starryTrailIntensity = builder
                    .comment("The intensity of the starry trail effect (0.0 - ...)",
                            "Default: 0.5")
                    .defineInRange("intensity", 0.5, 0.0, Float.MAX_VALUE);

            starryTrailStarScale = builder
                    .comment("The scale of individual stars in the trail (0.5 - ...)",
                            "Default: 1.2")
                    .defineInRange("starScale", 1.2, 0.5, Float.MAX_VALUE);

            starryTrailOpacity = builder
                    .comment("The opacity of the starry trail effect (0.0 - ...)",
                            "Default: 0.2")
                    .defineInRange("opacity", 0.2, 0.0, Float.MAX_VALUE);

            starryTrailLayers = builder
                    .comment("The number of layers for the starry trail effect (1 - ...)",
                            "Default: 1")
                    .defineInRange("layers", 1, 1, Integer.MAX_VALUE);

            starryTrailTexture = builder
                    .comment("The texture resource location for stars",
                            "Format: modid:path/to/texture.png",
                            "Default: fancy_trail:textures/effect/star.png")
                    .define("texture", "fancy_trail:textures/effect/star.png");

            builder.pop();

            builder.push("chromatic_aberration");

            chromaticEffect = builder
                    .comment("The strength of chromatic aberration effect (0.0 - ...)",
                            "0.0 = No effect, 1.0 = Maximum effect",
                            "Default: 0.3")
                    .defineInRange("chromaticEffect", 2.4, 0.0, Float.MAX_VALUE);

            builder.pop();

            builder.push("additional_particles");

            additionalParticleSpeed = builder
                    .comment("The speed multiplier for additional particles (0.0 - ...)",
                            "0.0 = No movement, 1.0 = Normal speed, 2.0 = Double speed",
                            "Default: 0.64")
                    .defineInRange("speed", 0.64, 0.0, Float.MAX_VALUE);

            additionalParticleType = builder
                    .comment("The particle type for additional effects",
                            "Format: modid:particle_registry_name",
                            "Examples: minecraft:end_rod, minecraft:portal, epicfight:blood, etc.",
                            "Default: minecraft:end_rod")
                    .define("particleType", "minecraft:end_rod");

            builder.pop();

            builder.push("flowing_effect");

            flowingIntensity = builder
                    .comment("The intensity of the flowing effect (0.0 - ...)",
                            "0.0 = No flowing, 0.1 = Subtle, 0.5 = Moderate, 1.0 = Strong",
                            "Default: 0.4")
                    .defineInRange("intensity", 0.4, 0.0, Float.MAX_VALUE);

            flowingSpeed = builder
                    .comment("The speed/frequency of the flowing animation (0.0 - ...)",
                            "0.0 = Static, 0.5 = Slow, 1.0 = Normal, 2.0 = Fast, 3.0 = Very Fast",
                            "Default: 0.8")
                    .defineInRange("speed", 0.8, 0.0, Float.MAX_VALUE);

            builder.pop();

            builder.push("common_air_trail");

            isOpenAir = builder
                    .comment("enable common air trail")
                    .define("isOpenAir", true);
            builder.pop();

            builder.push("weapon_afterimage");

            weaponAfterimageEnabled = builder
                    .comment("Enable weapon 3D model ghost afterimages during attacks",
                            "Default: true")
                    .define("enabled", true);

            weaponAfterimageGhostCount = builder
                    .comment("Number of ghost copies trailing behind the weapon",
                            "Higher values = longer trail but more GPU work",
                            "Range: 1 - 16",
                            "Default: 5")
                    .defineInRange("ghostCount", 5, 1, 16);

            weaponAfterimageMaxAlpha = builder
                    .comment("Maximum opacity of the ghost copies (oldest ghosts are most transparent)",
                            "Range: 0.0 - 1.0",
                            "Default: 0.4")
                    .defineInRange("maxAlpha", 0.4, 0.0, 1.0);

            weaponAfterimageFadeExponent = builder
                    .comment("Controls how quickly ghosts fade out",
                            "1.0 = linear fade, 2.0 = quadratic (faster fade), 0.5 = square root (slower fade)",
                            "Range: 0.5 - 5.0",
                            "Default: 2.0")
                    .defineInRange("fadeExponent", 2.0, 0.5, 5.0);

            weaponAfterimageMaxAgeTicks = builder
                    .comment("Maximum age of a ghost snapshot in ticks (20 ticks = 1 second)",
                            "Range: 2 - 40",
                            "Default: 10")
                    .defineInRange("maxAgeTicks", 10, 2, 40);

            builder.pop();
        }
    }

    // 1.21 NeoForge: config registration moved onto the ModContainer (was ModLoadingContext.get()).
    public static void init(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, "fancy_trail-client.toml");
    }

    public static float getIntensity() {
        return CLIENT.starryTrailIntensity.get().floatValue();
    }

    public static float getStarScale() {
        return CLIENT.starryTrailStarScale.get().floatValue();
    }

    public static float getOpacity() {
        return CLIENT.starryTrailOpacity.get().floatValue();
    }

    public static int getLayers() {
        return CLIENT.starryTrailLayers.get();
    }

    public static ResourceLocation getStarTexture() {
        String textureString = CLIENT.starryTrailTexture.get();
        try {
            String[] parts = textureString.split(":");
            if (parts.length == 2) {
                return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
            }
        } catch (Exception e) {
            // fall through to the default below on parse failure
        }
        return ResourceLocation.fromNamespaceAndPath("fancy_trail", "textures/effect/star.png");
    }

    public static float getChromaticEffect() {
        return CLIENT.chromaticEffect.get().floatValue();
    }

    public static float getAdditionalParticleSpeed() {
        return CLIENT.additionalParticleSpeed.get().floatValue();
    }

    public static SimpleParticleType getAdditionalParticleType() {
        String particleTypeString = CLIENT.additionalParticleType.get();

        try {
            ResourceLocation particleLocation = ResourceLocation.tryParse(particleTypeString);
            if (particleLocation != null) {
                // 1.21 NeoForge: ForgeRegistries.PARTICLE_TYPES -> vanilla BuiltInRegistries.PARTICLE_TYPE.
                ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.get(particleLocation);
                if (particleType instanceof SimpleParticleType simpleParticleType) {
                    return simpleParticleType;
                } else if (particleType != null) {
                    FT.LOGGER.warn("Particle type {} is not a SimpleParticleType, using default", particleTypeString);
                } else {
                    FT.LOGGER.warn("Particle type {} not found in registry, using default", particleTypeString);
                }
            }
        } catch (Exception e) {
            FT.LOGGER.error("Failed to parse particle type: {}", particleTypeString, e);
        }

        return ParticleTypes.END_ROD;
    }

    public static float getflowingIntensity() {
        return CLIENT.flowingIntensity.get().floatValue();
    }

    public static float getflowingSpeed() {
        return CLIENT.flowingSpeed.get().floatValue();
    }

    /**
     * Registry name of the additional-particle type (display/validation).
     */
    public static String getAdditionalParticleTypeString() {
        return CLIENT.additionalParticleType.get();
    }

    /**
     * Whether the given particle type id resolves to a SimpleParticleType.
     */
    public static boolean isValidParticleType(String particleTypeString) {
        try {
            ResourceLocation particleLocation = ResourceLocation.tryParse(particleTypeString);
            if (particleLocation != null) {
                ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.get(particleLocation);
                return particleType instanceof SimpleParticleType;
            }
        } catch (Exception e) {
            // ignored
        }
        return false;
    }

    public static boolean getAirIsOpen() {
        return CLIENT.isOpenAir.get();
    }

    public static boolean getWeaponAfterimageEnabled() {
        return CLIENT.weaponAfterimageEnabled.get();
    }

    public static int getWeaponAfterimageGhostCount() {
        return CLIENT.weaponAfterimageGhostCount.get();
    }

    public static float getWeaponAfterimageMaxAlpha() {
        return CLIENT.weaponAfterimageMaxAlpha.get().floatValue();
    }

    public static float getWeaponAfterimageFadeExponent() {
        return CLIENT.weaponAfterimageFadeExponent.get().floatValue();
    }

    public static int getWeaponAfterimageMaxAgeTicks() {
        return CLIENT.weaponAfterimageMaxAgeTicks.get();
    }


    public static void setChromaticEffect(float value) {
        CLIENT.chromaticEffect.set((double) value);
    }

    public static void setIntensity(float value) {
        CLIENT.starryTrailIntensity.set((double) value);
    }

    public static void setStarScale(float value) {
        CLIENT.starryTrailStarScale.set((double) value);
    }

    public static void setOpacity(float value) {
        CLIENT.starryTrailOpacity.set((double) value);
    }

    public static void setLayers(int value) {
        CLIENT.starryTrailLayers.set(value);
    }

    public static void setStarTexture(String texture) {
        CLIENT.starryTrailTexture.set(texture);
    }

    public static void setAdditionalParticleSpeed(float value) {
        CLIENT.additionalParticleSpeed.set((double) value);
    }

    public static void setAdditionalParticleType(String particleType) {
        CLIENT.additionalParticleType.set(particleType);
    }

    public static void setflowingIntensity(float value) {
        CLIENT.flowingIntensity.set((double) value);
    }

    public static void setflowingSpeed(float value) {
        CLIENT.flowingSpeed.set((double) value);
    }
}
