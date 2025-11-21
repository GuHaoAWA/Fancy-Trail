package com.guhao.fancy_trail.client.render;


import com.google.common.collect.Maps;
import com.guhao.fancy_trail.FT;
import com.guhao.fancy_trail.client.render.custom.*;
import com.guhao.fancy_trail.unit.OjangUtils;
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

import static com.guhao.fancy_trail.unit.RenderUtils.GetTexture;


@OnlyIn(Dist.CLIENT)
public class FTRenderType {
    public static final ResourceLocation NoneTexture = GetTexture("none");
    public static final HashMap<ResourceLocation, BloomParticleRenderType> BloomRenderTypes = Maps.newHashMap();
    public static SpaceTrailRenderType spaceTrailRenderType(ResourceLocation location) {
        return new SpaceTrailRenderType(OjangUtils.newRL(FT.MODID, "space_broken_end"), location, 1, 4);
    }

    public static AirDisturbanceRenderType airDisturbanceRenderType(ResourceLocation texture) {
        return new AirDisturbanceRenderType(OjangUtils.newRL(FT.MODID, "air_trail"),
                texture,
                0.2f,  // 强度
                0.1f,   // 速度
                1.0f,   // 方向X
                0.0f,   // 方向Y
                0.5f    // 刀身长度
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
        if (BloomRenderTypes.containsKey(texture)) {
            return (BloomTrailRenderType) BloomRenderTypes.get(texture);
        } else {
            BloomTrailRenderType bloomType = new BloomTrailRenderType(OjangUtils.newRL(FT.MODID, "bt_" + bloomIdx++), texture);
            BloomRenderTypes.put(texture, bloomType);
            return bloomType;
        }
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
