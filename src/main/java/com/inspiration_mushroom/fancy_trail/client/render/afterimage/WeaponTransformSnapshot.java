package com.inspiration_mushroom.fancy_trail.client.render.afterimage;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

public class WeaponTransformSnapshot {
    public final OpenMatrix4f jointLocalTransform;
    public final Pose animationPose;
    public final Vec3 entityPosition;
    public final float entityYRot;
    public final ItemStack weaponStack;
    public final InteractionHand hand;
    public final int jointId;
    public final long gameTime;

    public WeaponTransformSnapshot(
            OpenMatrix4f jointLocalTransform,
            Pose animationPose,
            Vec3 entityPosition,
            float entityYRot,
            ItemStack weaponStack,
            InteractionHand hand,
            int jointId,
            long gameTime) {
        // EF reuses the matrix and the Vec3 across frames, so both have to be copied out here
        this.jointLocalTransform = new OpenMatrix4f(jointLocalTransform);
        this.animationPose = animationPose;
        this.entityPosition = new Vec3(entityPosition.x, entityPosition.y, entityPosition.z);
        this.entityYRot = entityYRot;
        this.weaponStack = weaponStack;
        this.hand = hand;
        this.jointId = jointId;
        this.gameTime = gameTime;
    }

    public long getAgeTicks(long currentGameTime) {
        return Math.max(0, currentGameTime - gameTime);
    }
}
