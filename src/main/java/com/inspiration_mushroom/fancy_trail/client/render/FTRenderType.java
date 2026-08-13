package com.inspiration_mushroom.fancy_trail.client.render;


import com.google.common.collect.Maps;
import com.inspiration_mushroom.fancy_trail.FT;
import com.inspiration_mushroom.fancy_trail.client.render.custom.*;
import com.guhao.vix.util.OjangUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;




@OnlyIn(Dist.CLIENT)
public class FTRenderType {
    public static ResourceLocation GetTexture(String path) {
        return ResourceLocation.fromNamespaceAndPath(FT.MODID, "textures/" + path + ".png");
    }
    public static final ResourceLocation NoneTexture = GetTexture("none");
    public static final HashMap<ResourceLocation, BloomParticleRenderType> BloomRenderTypes = Maps.newHashMap();
    private static final HashMap<ResourceLocation, HashMap<Integer, BloomTrailRenderType>> BloomRenderTypesByIntensity = Maps.newHashMap();
    private static final HashMap<ResourceLocation, BackgroundBlendRenderType> BackgroundBlendCache = Maps.newHashMap();
    private static int backgroundBlendIdx = 0;

    public static SpaceTrailRenderType spaceTrailRenderType(ResourceLocation location) {
        return new SpaceTrailRenderType(OjangUtils.newRL(FT.MODID, "space_broken_end"), location, 1, 4);
    }

    public static AirDisturbanceRenderType airDisturbanceRenderType(ResourceLocation texture) {
        return new AirDisturbanceRenderType(OjangUtils.newRL(FT.MODID, "air_trail"),
                texture
        );
    }

    public static StaticAirDisturbanceRenderType staticAirDisturbanceRenderType(ResourceLocation texture) {
        return new StaticAirDisturbanceRenderType(OjangUtils.newRL(FT.MODID, "static_air_trail"),
                texture
        );
    }

    public static ChromaticAberrationRenderType strongChromaticAberrationRenderType(ResourceLocation location) {
        return ChromaticAberrationRenderType.createWithOffset(
                OjangUtils.newRL(FT.MODID, "chromatic_aberration"),
                location,
                1.01f, 1.002f, 1.008f  // stronger chromatic aberration
        );
    }

    private static int bloomIdx = 0;

    public static RGBTrailRenderType RGBTrailRenderType(ResourceLocation location) {
        return RGBTrailRenderType.createDefault(
                OjangUtils.newRL(FT.MODID, "rgb_trail"),
                location
        );
    }

    public static StarryTrailRenderType subtleStarryTrailRenderType(ResourceLocation location, ResourceLocation starTexture) {
        return StarryTrailRenderType.createSubtle(
                OjangUtils.newRL(FT.MODID, "subtle_starry_trail"),
                location,
                starTexture
        );
    }

    // custom-parameter variant
    public static StarryTrailRenderType customStarryTrailRenderType(ResourceLocation location,
                                                                    float intensity, float starScale,
                                                                    float opacity, int layers,
                                                                    ResourceLocation starTexture) {
        return StarryTrailRenderType.createCustom(
                OjangUtils.newRL(FT.MODID, "custom_starry_trail"),
                location,
                intensity, starScale, opacity, layers,
                starTexture
        );
    }

    public static BloomTrailRenderType getBloomTrailRT(ResourceLocation texture) {
        return getBloomTrailRT(texture, BloomParticleRenderType.DEFAULT_BLOOM_INTENSITY);
    }

    public static BloomTrailRenderType getBloomTrailRT(ResourceLocation texture, float bloomIntensity) {
        float sanitizedIntensity = BloomSettings.sanitizeIntensity(bloomIntensity);
        int intensityKey = Float.floatToIntBits(sanitizedIntensity);
        HashMap<Integer, BloomTrailRenderType> renderTypes = BloomRenderTypesByIntensity.computeIfAbsent(texture, ignored -> Maps.newHashMap());

        BloomTrailRenderType bloomType = renderTypes.get(intensityKey);
        if (bloomType == null) {
            bloomType = new BloomTrailRenderType(OjangUtils.newRL(FT.MODID, "bt_" + bloomIdx++), texture, sanitizedIntensity);
            renderTypes.put(intensityKey, bloomType);
            BloomRenderTypes.putIfAbsent(texture, bloomType);
        }
        return bloomType;
    }

    // after-image effect tuned for blade trails
    public static AfterImageRenderType bladeAfterImageRenderType(ResourceLocation texture) {
        return new AfterImageRenderType(
                OjangUtils.newRL(FT.MODID, "blade_after_image"),
                texture,
                0.6f,    // strength - higher makes the after-image more visible
                4.0f,    // decay speed - faster makes the after-image sharper
                0.8f,    // motion blur factor - accentuates the blade arc
                0.15f,   // color shift - produces the blue/violet tint
                1.0f,    // direction X
                0.0f     // direction Y
        );
    }

    // fast slash blade trail
    public static AfterImageRenderType rapidSlashRenderType(ResourceLocation texture) {
        return new AfterImageRenderType(
                OjangUtils.newRL(FT.MODID, "rapid_slash"),
                texture,
                0.8f,    // higher strength
                5.0f,    // faster decay (shorter after-image)
                0.9f,    // strong motion blur
                0.2f,    // strong color shift
                1.0f,
                0.0f
        );
    }

    // heavy blow blade trail (longer tail)
    public static AfterImageRenderType heavyBlowRenderType(ResourceLocation texture) {
        return new AfterImageRenderType(
                OjangUtils.newRL(FT.MODID, "heavy_blow"),
                texture,
                0.5f,    // moderate strength
                2.5f,    // slower decay (longer tail)
                0.6f,    // moderate motion blur
                0.1f,    // slight color shift
                1.0f,
                0.0f
        );
    }
    public static BackgroundBlendRenderType backgroundBlendRenderType(ResourceLocation texture) {
        ResourceLocation key = OjangUtils.newRL(FT.MODID, "background_blend_default");
        if (!BackgroundBlendCache.containsKey(key)) {
            BackgroundBlendCache.put(key, BackgroundBlendRenderType.create(texture));
        }
        return BackgroundBlendCache.get(key);
    }

    /**
     * Background-blend render type with custom parameters.
     */
    public static BackgroundBlendRenderType backgroundBlendRenderType(ResourceLocation texture,
                                                                      float blendStrength,
                                                                      float glowIntensity,
                                                                      float alphaBoost) {
        ResourceLocation key = OjangUtils.newRL(FT.MODID, "background_blend_custom_" + backgroundBlendIdx++);
        BackgroundBlendRenderType renderType = BackgroundBlendRenderType.createWithStrength(
                texture, blendStrength, glowIntensity, alphaBoost);
        BackgroundBlendCache.put(key, renderType);
        return renderType;
    }

    /**
     * Soft background-blend render type.
     */
    public static BackgroundBlendRenderType softBackgroundBlendRenderType(ResourceLocation texture) {
        ResourceLocation key = OjangUtils.newRL(FT.MODID, "background_blend_soft");
        if (!BackgroundBlendCache.containsKey(key)) {
            BackgroundBlendCache.put(key, BackgroundBlendRenderType.createWithStrength(texture, 0.6f, 0.8f, 1.0f));
        }
        return BackgroundBlendCache.get(key);
    }

    /**
     * Strong background-blend render type.
     */
    public static BackgroundBlendRenderType strongBackgroundBlendRenderType(ResourceLocation texture) {
        ResourceLocation key = OjangUtils.newRL(FT.MODID, "background_blend_strong");
        if (!BackgroundBlendCache.containsKey(key)) {
            BackgroundBlendCache.put(key, BackgroundBlendRenderType.createWithStrength(texture, 1.0f, 1.5f, 1.5f));
        }
        return BackgroundBlendCache.get(key);
    }

    /**
     * Glow-boosted background-blend render type.
     */
    public static BackgroundBlendRenderType glowingBackgroundBlendRenderType(ResourceLocation texture) {
        ResourceLocation key = OjangUtils.newRL(FT.MODID, "background_blend_glow");
        if (!BackgroundBlendCache.containsKey(key)) {
            BackgroundBlendCache.put(key, BackgroundBlendRenderType.createWithStrength(texture, 0.8f, 1.8f, 1.2f));
        }
        return BackgroundBlendCache.get(key);
    }


    public static ParticleRenderType createEnderPortalRenderType() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture endPortalTexture = textureManager.getTexture(TheEndPortalRenderer.END_PORTAL_LOCATION);

        // 1.21 ParticleRenderType: begin(Tesselator, TextureManager) RETURNS the builder and
        // there is no end() hook — the engine builds/draws the batch and restores depth mask +
        // blend itself after the type loop (the upstream end() body).
        return new ParticleRenderType() {
            @Override
            public BufferBuilder begin(@NotNull Tesselator tesselator, @NotNull TextureManager textureManager) {
                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.depthMask(false);

                // Bind the end-portal texture and clamp it
                RenderSystem.setShaderTexture(0, TheEndPortalRenderer.END_PORTAL_LOCATION);
                RenderSystem.texParameter(3553, 10242, 33071); // GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE
                RenderSystem.texParameter(3553, 10243, 33071); // GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE

                RenderSystem.setShader(GameRenderer::getParticleShader);

                return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            }

            @Override
            public String toString() {
                return "ender_portal_particle";
            }
        };
    }
}