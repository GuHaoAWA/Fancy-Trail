package com.guhao.fancy_trail.client.render.afterimage;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

/**
 * 存储武器在某个时刻的完整渲染状态，用于后续渲染幽灵残影。
 * 所有字段在捕获时设置，之后只读。
 */
public class WeaponTransformSnapshot {
    /** 关节在模型空间中的变换矩阵（从 Armature.getBoundTransformFor 获取） */
    public final OpenMatrix4f jointLocalTransform;

    /** 捕获时刻实体的动画 Pose（用于重建 Armature 关节矩阵） */
    public final Pose animationPose;

    /** 捕获时刻实体的世界坐标 */
    public final Vec3 entityPosition;

    /** 捕获时刻实体的 Y 轴旋转（度数） */
    public final float entityYRot;

    /** 武器的 ItemStack 引用 */
    public final ItemStack weaponStack;

    /** 哪个手持握武器 */
    public final InteractionHand hand;

    /** 武器关节的 ID */
    public final int jointId;

    /** 捕获时的游戏时间（tick），用于计算残影年龄 */
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
        // 深拷贝矩阵，避免后续动画更新影响历史数据
        this.jointLocalTransform = new OpenMatrix4f(jointLocalTransform);
        this.animationPose = animationPose;
        this.entityPosition = new Vec3(entityPosition.x, entityPosition.y, entityPosition.z);
        this.entityYRot = entityYRot;
        this.weaponStack = weaponStack;
        this.hand = hand;
        this.jointId = jointId;
        this.gameTime = gameTime;
    }

    /**
     * 计算此快照的年龄（tick）
     */
    public long getAgeTicks(long currentGameTime) {
        return Math.max(0, currentGameTime - gameTime);
    }
}
