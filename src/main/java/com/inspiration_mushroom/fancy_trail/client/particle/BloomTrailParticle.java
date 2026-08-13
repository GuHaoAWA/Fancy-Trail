package com.inspiration_mushroom.fancy_trail.client.particle;

import com.inspiration_mushroom.fancy_trail.client.render.FTRenderType;
import com.inspiration_mushroom.fancy_trail.client.render.custom.BloomSettings;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class BloomTrailParticle extends AnimationTrailParticle {
    private final BloomSettings bloomSettings;

    protected BloomTrailParticle(ClientLevel level, LivingEntityPatch<?> owner, Joint joint, AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo) {
        this(level, owner, joint, animation, trailInfo, BloomSettings.get(trailInfo));
    }

    protected BloomTrailParticle(ClientLevel level, LivingEntityPatch<?> owner, Joint joint, AssetAccessor<? extends StaticAnimation> animation,
                                 TrailInfo trailInfo, BloomSettings bloomSettings) {
        super(level, owner, joint, animation, trailInfo);
        this.bloomSettings = bloomSettings;
        applyBloomColor();
    }

    private void applyBloomColor() {
        if (bloomSettings != BloomSettings.DEFAULT) {
            this.rCol = bloomSettings.r;
            this.gCol = bloomSettings.g;
            this.bCol = bloomSettings.b;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return FTRenderType.getBloomTrailRT(trailInfo.texturePath(), bloomSettings.intensity);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet spriteSet) {
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            int eid = (int) Double.doubleToRawLongBits(x);
            int animid = (int) Double.doubleToRawLongBits(z);
            int jointId = (int) Double.doubleToRawLongBits(xSpeed);
            int idx = (int) Double.doubleToRawLongBits(ySpeed);
            Entity entity = level.getEntity(eid);

            if (entity == null) {
                return null;
            }

            LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            if (entitypatch == null) {
                return null;
            }

            AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = AnimationManager.byId(animid);

            if (animation == null) {
                return null;
            }

            Optional<List<TrailInfo>> trailInfo = animation.get().getProperty(ClientAnimationProperties.TRAIL_EFFECT);

            if (trailInfo.isEmpty()) {
                return null;
            }

            TrailInfo result = trailInfo.get().get(idx);
            BloomSettings bloomSettings = BloomSettings.find(result);

            if (result.hand() != null) {
                ItemStack stack = entitypatch.getOriginal().getItemInHand(result.hand());
                RenderItemBase renderItemBase = RenderEngine.getInstance().getItemRenderer(stack);

                if (renderItemBase != null && renderItemBase.trailInfo() != null) {
                    TrailInfo itemTrailInfo = renderItemBase.trailInfo();
                    if (bloomSettings == null) {
                        bloomSettings = BloomSettings.find(itemTrailInfo);
                    }
                    result = itemTrailInfo.overwrite(result);
                }
            }
            result = entitypatch.getEntityDecorations().getModifiedTrailInfo(result, result.hand() == null ? CapabilityItem.EMPTY : entitypatch.getAdvancedHoldingItemCapability(result.hand()));
            if (result.playable()) {
                if (bloomSettings == null) {
                    bloomSettings = BloomSettings.get(result);
                }
                return new BloomTrailParticle(level, entitypatch, entitypatch.getArmature().searchJointById(jointId), animation, result, bloomSettings);
            } else {
                return null;
            }
        }
    }

}