package com.guhao.fancy_trail.client.particle;


import com.google.common.collect.Lists;
import com.guhao.fancy_trail.unit.RenderUtils;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.physics.bezier.CubicBezierCurve;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.Optional;


@OnlyIn(Dist.CLIENT)
public class BrustTrailParticle extends AnimationTrailParticle {

    protected BrustTrailParticle(ClientLevel level, LivingEntityPatch<?> owner, Joint joint, AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo) {
        super(level, owner, joint, animation, trailInfo);
    }
    protected void createNextCurve() {
        AnimationPlayer animPlayer = this.owner.getAnimator().getPlayerFor(this.animation);
        boolean isTrailInvisible = animPlayer.getAnimation().get().isLinkAnimation() || animPlayer.getElapsedTime() <= this.trailInfo.startTime();
        boolean isFirstTrail = this.trailEdges.isEmpty();
        boolean needCorrection = (!isTrailInvisible && isFirstTrail);

        if (needCorrection) {
            float startCorrection = Math.max((this.trailInfo.startTime() - animPlayer.getPrevElapsedTime()) / (animPlayer.getElapsedTime() - animPlayer.getPrevElapsedTime()), 0.0F);
            this.startEdgeCorrection = this.trailInfo.interpolateCount() * 2 * startCorrection;
        }

        TrailInfo trailInfo = this.trailInfo;
        Pose prevPose = this.owner.getAnimator().getPose(0.0F);//this.lastPose;
        Pose currentPose = this.owner.getAnimator().getPose(1.0F);
        Pose middlePose = this.owner.getAnimator().getPose(0.5F);//Pose.interpolatePose(prevPose, currentPose, 0.5F);

        Vec3 posOld = this.owner.getOriginal().getPosition(0.0F);
        Vec3 posCur = this.owner.getOriginal().getPosition(1.0F);
        Vec3 posMid = MathUtils.lerpVector(posOld, posCur, 0.5F);

        OpenMatrix4f prevModelMatrix = this.owner.getModelMatrix(0.0F);
        OpenMatrix4f curModelMatrix = this.owner.getModelMatrix(1.0F);
        JointTransform lastTransform = JointTransform.fromMatrix(curModelMatrix);
        JointTransform currentTransform = JointTransform.fromMatrix(curModelMatrix);

        OpenMatrix4f prvmodelTf
                = OpenMatrix4f
                .createTranslation((float)posOld.x, (float)posOld.y, (float)posOld.z)
                .rotateDeg(180.0F, Vec3f.Y_AXIS)
                .mulBack(prevModelMatrix);
        OpenMatrix4f middleModelTf
                = OpenMatrix4f
                .createTranslation((float)posMid.x, (float)posMid.y, (float)posMid.z)
                .rotateDeg(180.0F, Vec3f.Y_AXIS)
                .mulBack(JointTransform.interpolate(lastTransform, currentTransform, 0.5F).toMatrix());
        OpenMatrix4f curModelTf
                = OpenMatrix4f
                .createTranslation((float)posCur.x, (float)posCur.y, (float)posCur.z)
                .rotateDeg(180.0F, Vec3f.Y_AXIS)
                .mulBack(curModelMatrix);

        OpenMatrix4f prevJointTf = this.owner.getArmature().getBoundTransformFor(prevPose, this.joint).mulFront(prvmodelTf);
        OpenMatrix4f middleJointTf = this.owner.getArmature().getBoundTransformFor(middlePose, this.joint).mulFront(middleModelTf);
        OpenMatrix4f currentJointTf = this.owner.getArmature().getBoundTransformFor(currentPose, this.joint).mulFront(curModelTf);
        Vec3 prevStartPos = OpenMatrix4f.transform(prevJointTf, trailInfo.start());
        Vec3 prevEndPos = OpenMatrix4f.transform(prevJointTf, trailInfo.end());
        Vec3 middleStartPos = OpenMatrix4f.transform(middleJointTf, trailInfo.start());
        Vec3 middleEndPos = OpenMatrix4f.transform(middleJointTf, trailInfo.end());
        Vec3 currentStartPos = OpenMatrix4f.transform(currentJointTf, trailInfo.start());
        Vec3 currentEndPos = OpenMatrix4f.transform(currentJointTf, trailInfo.end());

        List<Vec3> finalStartPositions;
        List<Vec3> finalEndPositions;
        boolean visibleTrail;

        if (isTrailInvisible) {
            finalStartPositions = Lists.newArrayList();
            finalEndPositions = Lists.newArrayList();
            finalStartPositions.add(prevStartPos);
            finalStartPositions.add(middleStartPos);
            finalEndPositions.add(prevEndPos);
            finalEndPositions.add(middleEndPos);

            this.invisibleTrailEdges.clear();
            visibleTrail = false;
        } else {
            List<Vec3> startPosList = Lists.newArrayList();
            List<Vec3> endPosList = Lists.newArrayList();
            TrailEdge edge1;
            TrailEdge edge2;

            if (isFirstTrail) {
                int lastIdx = this.invisibleTrailEdges.size() - 1;
                edge1 = this.invisibleTrailEdges.get(lastIdx);
                edge2 = new TrailEdge(prevStartPos, prevEndPos, -1);
            } else {
                edge1 = this.trailEdges.get(this.trailEdges.size() - (this.trailInfo.interpolateCount() / 2 + 1));
                edge2 = this.trailEdges.get(this.trailEdges.size() - 1);
                edge2.lifetime++;
            }

            startPosList.add(edge1.start);
            endPosList.add(edge1.end);
            startPosList.add(edge2.start);
            endPosList.add(edge2.end);
            startPosList.add(middleStartPos);
            endPosList.add(middleEndPos);
            startPosList.add(currentStartPos);
            endPosList.add(currentEndPos);

            finalStartPositions = CubicBezierCurve.getBezierInterpolatedPoints(startPosList, 1, 3, this.trailInfo.interpolateCount());
            finalEndPositions = CubicBezierCurve.getBezierInterpolatedPoints(endPosList, 1, 3, this.trailInfo.interpolateCount());

            if (!isFirstTrail) {
                finalStartPositions.remove(0);
                finalEndPositions.remove(0);
            }

            visibleTrail = true;
        }
        RenderUtils.spawnAdditionalParticles(level,finalStartPositions,finalEndPositions);
        this.makeTrailEdges(finalStartPositions, finalEndPositions, visibleTrail ? this.trailEdges : this.invisibleTrailEdges);


    }



    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        super.render(pBuffer, pRenderInfo, pPartialTicks);

    }




    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.NO_RENDER;
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

            if (result.hand() != null) {
                ItemStack stack = entitypatch.getOriginal().getItemInHand(result.hand());
                RenderItemBase renderItemBase = ClientEngine.getInstance().renderEngine.getItemRenderer(stack);

                if (renderItemBase != null && renderItemBase.trailInfo() != null) {
                    result = renderItemBase.trailInfo().overwrite(result);
                }
            }

            if (result.playable()) {
                return new BrustTrailParticle(level, entitypatch, entitypatch.getArmature().searchJointById(jointId), animation, result);
            } else {
                return null;
            }
        }
    }
}
