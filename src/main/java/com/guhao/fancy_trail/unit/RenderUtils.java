package com.guhao.fancy_trail.unit;

import com.guhao.fancy_trail.FT;
import com.guhao.fancy_trail.FTClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class RenderUtils {

    public static void GLSetTexture(ResourceLocation texture) {
        TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstracttexture = texturemanager.getTexture(texture);
        RenderSystem.bindTexture(abstracttexture.getId());
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.setShaderTexture(0, abstracttexture.getId());
    }

    public static ResourceLocation GetTexture(String path) {
        return ResourceLocation.fromNamespaceAndPath(FT.MODID, "textures/" + path + ".png");
    }
    @OnlyIn(Dist.CLIENT)
    public static void delay_particle(ClientLevel level, int eid, int animid, int jointId, int idx,
                                      Long delayTime, SimpleParticleType simpleParticleType) {
        ClientParticleDelayerUnit.scheduleParticle(level, eid, animid, jointId, idx, delayTime, simpleParticleType);
    }
    public static void spawnAdditionalParticles(Level level, List<Vec3> startPositions, List<Vec3> endPositions) {
        for (int i = 0; i < startPositions.size(); i++) {
            Vec3 startPos = startPositions.get(i);
            Vec3 endPos = endPositions.get(i);
            Vec3 direction = endPos.subtract(startPos).normalize();
            double speed = FTClientConfig.getAdditionalParticleSpeed();

            for (int j = 0; j < 1; j++) {
                Vec3 particlePos = startPos.add(direction.scale(j * 0.5));
                level.addParticle(FTClientConfig.getAdditionalParticleType(), true,
                        particlePos.x, particlePos.y, particlePos.z,
                        speed * direction.x, speed * direction.y, speed * direction.z);

            }
        }
    }

}
