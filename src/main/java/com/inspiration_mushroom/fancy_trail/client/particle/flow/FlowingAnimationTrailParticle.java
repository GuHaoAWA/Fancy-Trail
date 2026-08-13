package com.inspiration_mushroom.fancy_trail.client.particle.flow;

import com.inspiration_mushroom.fancy_trail.FTClientConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4f;
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
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class FlowingAnimationTrailParticle extends AnimationTrailParticle {

    protected float flowSpeed = 3.0F;
    protected float flowOffset = 0.0F;

    private static final Random RANDOM = new Random();
    private static final float MAX_DISTORTION = 0.45f;
    private static final float MOTION_FACTOR_MULTIPLIER = 1.6f;

    protected float motionFactor = 0f;
    protected float timeOffset = RANDOM.nextFloat() * 100f;

    protected FlowingAnimationTrailParticle(ClientLevel level, LivingEntityPatch<?> owner, Joint joint, AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo) {
        super(level, owner, joint, animation, trailInfo);
    }

    @Override
    public void tick() {
        super.tick();


    }

    private void updateMotionFactor() {
        if (this.owner != null && this.owner.getOriginal() != null) {
            net.minecraft.world.phys.Vec3 deltaMovement = this.owner.getOriginal().getDeltaMovement();
            this.motionFactor = Mth.lerp(0.15f, this.motionFactor,
                    (float) deltaMovement.length() * 0.7f);
        }
    }

    private float calculateDynamicIntensity() {
        float baseIntensity = FTClientConfig.getflowingIntensity();
        float intensity = baseIntensity * (0.5f + motionFactor * MOTION_FACTOR_MULTIPLIER);
        return Mth.clamp(intensity, 0.05f, MAX_DISTORTION);
    }

    private Vector4f applyVertexDistortion(net.minecraft.world.phys.Vec3 position, float time, float intensity, Matrix4f matrix) {

        float speedMultiplier = FTClientConfig.getflowingSpeed();
        float timeFactor = time * speedMultiplier;

        float x = (float) position.x;
        float y = (float) position.y;
        float z = (float) position.z;

        float offsetX = Mth.sin(timeFactor + x * 0.5f) * intensity
                + Mth.cos(timeFactor * 0.8f + z * 0.3f) * intensity * 0.6f;

        float offsetY = Mth.sin(timeFactor * 1.2f + x * 0.2f) * intensity * 0.4f;

        float offsetZ = Mth.cos(timeFactor * 0.7f + z * 0.4f) * intensity
                - Mth.sin(timeFactor * 0.9f) * intensity * 0.3f;

        return new Vector4f(
                x + offsetX,
                y + offsetY,
                z + offsetZ,
                1.0F
        ).mul(matrix);
    }

    // motion blur
    private void applyMotionBlur(Vector4f pos1, Vector4f pos2, Vector4f pos3, Vector4f pos4, int index, int totalEdges) {
        if (this.owner != null && this.owner.getOriginal() != null) {
            net.minecraft.world.phys.Vec3 velocity = this.owner.getOriginal().getDeltaMovement();
            float velocityFactor = 0.2f * (1.0f - (float) index / totalEdges);

            Vector4f velocityOffset = new Vector4f(
                    (float) velocity.x * velocityFactor,
                    (float) velocity.y * velocityFactor,
                    (float) velocity.z * velocityFactor,
                    0
            );

            pos1.add(velocityOffset);
            pos2.add(velocityOffset);
            pos3.add(velocityOffset);
            pos4.add(velocityOffset);
        }
    }

    @Override
    public void render(com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, net.minecraft.client.Camera camera, float partialTick) {
        if (!this.trailEdges.isEmpty()) {
            com.mojang.blaze3d.vertex.PoseStack poseStack = new com.mojang.blaze3d.vertex.PoseStack();
            int light = this.getLightColor(partialTick);
            this.setupPoseStack(poseStack, camera, partialTick);
            Matrix4f matrix4f = poseStack.last().pose();
            int edges = this.trailEdges.size() - 1;
            boolean startFade = this.trailEdges.get(0).lifetime == 1;
            boolean endFade = this.trailEdges.get(edges).lifetime == this.trailInfo.trailLifetime();
            float startEdge = (startFade ? (float)(this.trailInfo.interpolateCount() * 2) * partialTick : 0.0F) + this.startEdgeCorrection;
            float endEdge = endFade ? Math.min((float)edges - (float)(this.trailInfo.interpolateCount() * 2) * (1.0F - partialTick), (float)(edges - 1)) : (float)(edges - 1);

            if (startEdge >= endEdge) {
                return;
            }

            float interval = 1.0F / (endEdge - startEdge);
            float fading = 1.0F;

            if (this.shouldRemove) {
                if (TrailInfo.isValidTime(this.trailInfo.fadeTime())) {
                    fading = (float)(this.lifetime - this.age) / (float)this.trailInfo.trailLifetime();
                } else {
                    fading = Mth.clamp(((float)(this.lifetime - this.age) + (1.0F - partialTick)) / (float)this.trailInfo.trailLifetime(), 0.0F, 1.0F);
                }
            }

            // dynamic effect parameters
            float time = (this.level.getGameTime() + partialTick + timeOffset) * 1.2f;
            float dynamicIntensity = calculateDynamicIntensity();
            float currentFlow = this.flowOffset;

            float partialStartEdge = interval * (startEdge % 1.0F);
            float from = -partialStartEdge;
            float to = -partialStartEdge + interval;

            for(int i = (int)startEdge; i < (int)endEdge + 1; ++i) {
                if (i >= this.trailEdges.size() - 1) break;

                TrailEdge e1 = this.trailEdges.get(i);
                TrailEdge e2 = this.trailEdges.get(i + 1);

                // apply the distortion to the vertices
                Vector4f pos1 = applyVertexDistortion(e1.start, time, dynamicIntensity, matrix4f);
                Vector4f pos2 = applyVertexDistortion(e1.end, time, dynamicIntensity, matrix4f);
                Vector4f pos3 = applyVertexDistortion(e2.end, time, dynamicIntensity, matrix4f);
                Vector4f pos4 = applyVertexDistortion(e2.start, time, dynamicIntensity, matrix4f);

                // apply motion blur
                applyMotionBlur(pos1, pos2, pos3, pos4, i, edges);

                // UV coordinates (flowing effect)
                float uvFrom = (from + currentFlow) % 1.0F;
                float uvTo = (to + currentFlow) % 1.0F;

                // keep UV within 0..1
                if (uvFrom < 0) uvFrom += 1.0F;
                if (uvTo < 0) uvTo += 1.0F;

                // use the parent's normal alpha computation
                float alphaFrom = Mth.clamp(from, 0.0F, 1.0F) * fading;
                float alphaTo = Mth.clamp(to, 0.0F, 1.0F) * fading;

                // original colors (dynamic color computation removed upstream)
                vertexConsumer.addVertex(pos1.x(), pos1.y(), pos1.z()).setUv(uvFrom, 1.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaFrom).setLight(light);
                vertexConsumer.addVertex(pos2.x(), pos2.y(), pos2.z()).setUv(uvFrom, 0.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaFrom).setLight(light);
                vertexConsumer.addVertex(pos3.x(), pos3.y(), pos3.z()).setUv(uvTo, 0.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaTo).setLight(light);
                vertexConsumer.addVertex(pos4.x(), pos4.y(), pos4.z()).setUv(uvTo, 1.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaTo).setLight(light);

                from += interval;
                to += interval;

                updateMotionFactor();
            }
        }
    }

    // flow speed setter
    public void setFlowSpeed(float speed) {
        this.flowSpeed = speed;
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
            LivingEntityPatch<?> owner = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            if (owner == null) {
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

            if (result.hand() != null) {
                ItemStack stack = owner.getOriginal().getItemInHand(result.hand());
                RenderItemBase renderItemBase = RenderEngine.getInstance().getItemRenderer(stack);

                if (renderItemBase != null && renderItemBase.trailInfo() != null) {
                    result = renderItemBase.trailInfo().overwrite(result);
                }
            }
            result = entitypatch.getEntityDecorations().getModifiedTrailInfo(result, result.hand() == null ? CapabilityItem.EMPTY : entitypatch.getAdvancedHoldingItemCapability(result.hand()));
            if (result.playable()) {
                FlowingAnimationTrailParticle particle = new FlowingAnimationTrailParticle(level, owner, owner.getArmature().searchJointById(jointId), animation, result);

                // tune the flow speed
                particle.setFlowSpeed(4.0F);

                return particle;
            } else {
                return null;
            }
        }
    }
}