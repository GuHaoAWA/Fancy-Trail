package com.inspiration_mushroom.fancy_trail.client.render;


import com.google.common.collect.Maps;
import com.inspiration_mushroom.fancy_trail.FT;
import com.guhao.vix.util.OjangUtils;
import com.inspiration_mushroom.fancy_trail.client.render.custom.*;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
                1.01f, 1.002f, 1.008f  // 更强的色差效果
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

    // 自定义参数版本
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

    // 刀光专用的残像效果
    public static AfterImageRenderType bladeAfterImageRenderType(ResourceLocation texture) {
        return new AfterImageRenderType(
                OjangUtils.newRL(FT.MODID, "blade_after_image"),
                texture,
                0.6f,    // 强度 - 更高让残影更明显
                4.0f,    // 衰减速度 - 更快让残影更锐利
                0.8f,    // 运动模糊因子 - 增强刀光轨迹
                0.15f,   // 色彩偏移 - 产生蓝紫效果
                1.0f,    // 方向X
                0.0f     // 方向Y
        );
    }

    // 高速斩击刀光
    public static AfterImageRenderType rapidSlashRenderType(ResourceLocation texture) {
        return new AfterImageRenderType(
                OjangUtils.newRL(FT.MODID, "rapid_slash"),
                texture,
                0.8f,    // 更高强度
                5.0f,    // 更快衰减（残影更短）
                0.9f,    // 强运动模糊
                0.2f,    // 强色彩偏移
                1.0f,
                0.0f
        );
    }

    // 重击刀光（拖尾更长）
    public static AfterImageRenderType heavyBlowRenderType(ResourceLocation texture) {
        return new AfterImageRenderType(
                OjangUtils.newRL(FT.MODID, "heavy_blow"),
                texture,
                0.5f,    // 适中强度
                2.5f,    // 较慢衰减（拖尾更长）
                0.6f,    // 适中运动模糊
                0.1f,    // 轻微色彩偏移
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
     * 获取自定义背景混合渲染类型
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
     * 获取柔和背景混合渲染类型
     */
    public static BackgroundBlendRenderType softBackgroundBlendRenderType(ResourceLocation texture) {
        ResourceLocation key = OjangUtils.newRL(FT.MODID, "background_blend_soft");
        if (!BackgroundBlendCache.containsKey(key)) {
            BackgroundBlendCache.put(key, BackgroundBlendRenderType.createWithStrength(texture, 0.6f, 0.8f, 1.0f));
        }
        return BackgroundBlendCache.get(key);
    }

    /**
     * 获取强烈背景混合渲染类型
     */
    public static BackgroundBlendRenderType strongBackgroundBlendRenderType(ResourceLocation texture) {
        ResourceLocation key = OjangUtils.newRL(FT.MODID, "background_blend_strong");
        if (!BackgroundBlendCache.containsKey(key)) {
            BackgroundBlendCache.put(key, BackgroundBlendRenderType.createWithStrength(texture, 1.0f, 1.5f, 1.5f));
        }
        return BackgroundBlendCache.get(key);
    }

    /**
     * 获取发光增强背景混合渲染类型
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

        return new ParticleRenderType() {
            public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.depthMask(false);

                // 绑定末地传送门纹理并设置纹理参数
                RenderSystem.setShaderTexture(0, TheEndPortalRenderer.END_PORTAL_LOCATION);
                RenderSystem.texParameter(3553, 10242, 33071); // GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE
                RenderSystem.texParameter(3553, 10243, 33071); // GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE

                // 设置粒子着色器
                RenderSystem.setShader(GameRenderer::getParticleShader);

                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            }

            public void end(@NotNull Tesselator tesselator) {
                tesselator.end();
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
                RenderSystem.enableCull();
            }

            public String toString() {
                return "ender_portal_particle";
            }
        };
    }
}
