package com.guhao.fancy_trail;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

public class FTClientConfig {
    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static class ClientConfig {
        public final ForgeConfigSpec.DoubleValue starryTrailIntensity;
        public final ForgeConfigSpec.DoubleValue starryTrailStarScale;
        public final ForgeConfigSpec.DoubleValue starryTrailOpacity;
        public final ForgeConfigSpec.IntValue starryTrailLayers;
        public final ForgeConfigSpec.ConfigValue<String> starryTrailTexture;

        public final ForgeConfigSpec.DoubleValue chromaticEffect;

        // 新增配置项
        public final ForgeConfigSpec.DoubleValue additionalParticleSpeed;
        public final ForgeConfigSpec.ConfigValue<String> additionalParticleType;

        // 新增：扰动效果配置
        public final ForgeConfigSpec.DoubleValue flowingIntensity;
        public final ForgeConfigSpec.DoubleValue flowingSpeed;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
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

            // 新增：扰动效果配置组
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
        }
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, "fancy_trail-client.toml");
    }

    // Starry Trail 获取配置值的方法
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
            // 如果解析失败，返回默认值
        }
        return ResourceLocation.fromNamespaceAndPath("fancy_trail", "textures/effect/star.png");
    }

    public static float getChromaticEffect() {
        return CLIENT.chromaticEffect.get().floatValue();
    }

    // 新增：获取附加粒子配置
    public static float getAdditionalParticleSpeed() {
        return CLIENT.additionalParticleSpeed.get().floatValue();
    }

    public static SimpleParticleType getAdditionalParticleType() {
        String particleTypeString = CLIENT.additionalParticleType.get();

        try {
            ResourceLocation particleLocation = ResourceLocation.tryParse(particleTypeString);
            if (particleLocation != null) {
                ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(particleLocation);
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

    // 新增：获取扰动效果配置
    public static float getflowingIntensity() {
        return CLIENT.flowingIntensity.get().floatValue();
    }

    public static float getflowingSpeed() {
        return CLIENT.flowingSpeed.get().floatValue();
    }

    /**
     * 获取粒子类型的注册名（用于显示或验证）
     */
    public static String getAdditionalParticleTypeString() {
        return CLIENT.additionalParticleType.get();
    }

    /**
     * 验证粒子类型是否存在
     */
    public static boolean isValidParticleType(String particleTypeString) {
        try {
            ResourceLocation particleLocation = ResourceLocation.tryParse(particleTypeString);
            if (particleLocation != null) {
                ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(particleLocation);
                return particleType instanceof SimpleParticleType;
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return false;
    }

    // 设置配置值的方法（用于运行时修改）
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

    // 新增：设置附加粒子配置
    public static void setAdditionalParticleSpeed(float value) {
        CLIENT.additionalParticleSpeed.set((double) value);
    }

    public static void setAdditionalParticleType(String particleType) {
        CLIENT.additionalParticleType.set(particleType);
    }

    // 新增：设置扰动效果配置
    public static void setflowingIntensity(float value) {
        CLIENT.flowingIntensity.set((double) value);
    }

    public static void setflowingSpeed(float value) {
        CLIENT.flowingSpeed.set((double) value);
    }
}