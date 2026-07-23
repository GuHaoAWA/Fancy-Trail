package com.inspiration_mushroom.fancy_trail.client.render.afterimage;

import com.inspiration_mushroom.fancy_trail.FTClientConfig;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理武器残像效果的全局单例。
 * <p>
 * 职责：
 * <ul>
 *   <li>追踪哪些实体正在播放武器拖尾动画</li>
 *   <li>为每个活跃实体维护一个武器变换历史的环形缓冲区</li>
 *   <li>提供 ThreadLocal 机制，让渲染时注入历史 modelMatrix</li>
 *   <li>在世界卸载/实体移除时清理数据</li>
 * </ul>
 */
public class WeaponAfterimageManager {

    // ===== 单例 =====
    private static final WeaponAfterimageManager INSTANCE = new WeaponAfterimageManager();

    public static WeaponAfterimageManager getInstance() {
        return INSTANCE;
    }

    private WeaponAfterimageManager() {}

    // ===== ThreadLocal 用于渲染时注入历史 modelMatrix =====
    /**
     * 在渲染幽灵武器之前设置此 ThreadLocal，让
     * LivingEntityPatchModelMatrixMixin 返回历史值。
     * 渲染后必须调用 {@link #clearRenderOverride()}。
     */
    public static final ThreadLocal<OpenMatrix4f> RENDER_OVERRIDE_MATRIX = new ThreadLocal<>();

    /**
     * 在渲染幽灵武器之前设置此 ThreadLocal，让
     * LivingEntityPatchModelMatrixMixin 返回历史 Pose。
     */
    public static final ThreadLocal<Pose> RENDER_OVERRIDE_POSE = new ThreadLocal<>();

    public static void setRenderOverride(OpenMatrix4f modelMatrix, Pose pose) {
        RENDER_OVERRIDE_MATRIX.set(modelMatrix);
        RENDER_OVERRIDE_POSE.set(pose);
    }

    public static void clearRenderOverride() {
        RENDER_OVERRIDE_MATRIX.remove();
        RENDER_OVERRIDE_POSE.remove();
    }

    // ===== 实体追踪 =====
    /**
     * 每个活跃实体的变换历史。使用 ConcurrentHashMap 以保证
     * 在渲染线程和粒子 tick 之间的线程安全（尽管 Minecraft
     * 主要是单线程渲染，但谨慎处理）。
     */
    private final Map<LivingEntityPatch<?>, WeaponAfterimageHistory> entityHistories = new ConcurrentHashMap<>();

    /**
     * 待注册的实体集合（粒子 mixin 检测到但尚未创建历史记录的实体）。
     */
    private final Set<LivingEntityPatch<?>> pendingRegistration = ConcurrentHashMap.newKeySet();

    /**
     * 上次活跃时间追踪（用于清除不活跃的实体）。
     */
    private final Map<LivingEntityPatch<?>, Long> lastActiveTime = new ConcurrentHashMap<>();

    // ===== 公共 API =====

    /**
     * 当 AnimationTrailParticleMixin 检测到武器拖尾时调用。
     * 将实体标记为待注册。实际注册延迟到下一个渲染 tick。
     */
    public void onWeaponTrailDetected(LivingEntityPatch<?> entityPatch) {
        if (entityPatch == null) return;
        if (!FTClientConfig.getWeaponAfterimageEnabled()) return;

        pendingRegistration.add(entityPatch);
    }

    /**
     * 捕获一个武器变换快照并存入对应实体的环形缓冲区。
     * 在 WeaponAfterimageCaptureMixin 中的每帧调用。
     */
    public void captureSnapshot(
            LivingEntityPatch<?> entityPatch,
            OpenMatrix4f jointLocalTransform,
            Pose animationPose,
            Vec3 entityPosition,
            float entityYRot,
            ItemStack weaponStack,
            InteractionHand hand,
            int jointId,
            long gameTime) {

        if (entityPatch == null || weaponStack == null || weaponStack.isEmpty()) return;
        if (!FTClientConfig.getWeaponAfterimageEnabled()) return;

        WeaponAfterimageHistory history = entityHistories.get(entityPatch);
        if (history == null) {
            // 首次捕获时自动创建历史记录
            int capacity = FTClientConfig.getWeaponAfterimageGhostCount() + 2; // +2 留余量
            history = new WeaponAfterimageHistory(capacity);
            entityHistories.put(entityPatch, history);
        }

        WeaponTransformSnapshot snapshot = new WeaponTransformSnapshot(
                jointLocalTransform,
                animationPose,
                entityPosition,
                entityYRot,
                weaponStack,
                hand,
                jointId,
                gameTime
        );
        history.push(snapshot);
        lastActiveTime.put(entityPatch, gameTime);
    }

    /**
     * 在渲染前调用，处理待注册实体并清理不活跃的实体。
     */
    public void update(long currentGameTime) {
        // 处理待注册实体 - 确保它们有历史记录
        if (!pendingRegistration.isEmpty()) {
            for (LivingEntityPatch<?> patch : pendingRegistration) {
                if (patch.getOriginal() == null || !patch.getOriginal().isAlive()) continue;
                entityHistories.putIfAbsent(
                        patch,
                        new WeaponAfterimageHistory(FTClientConfig.getWeaponAfterimageGhostCount() + 2)
                );
                lastActiveTime.putIfAbsent(patch, currentGameTime);
            }
            pendingRegistration.clear();
        }

        // 清理不活跃的实体
        int maxAgeTicks = FTClientConfig.getWeaponAfterimageMaxAgeTicks();
        Iterator<Map.Entry<LivingEntityPatch<?>, Long>> it = lastActiveTime.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<LivingEntityPatch<?>, Long> entry = it.next();
            LivingEntityPatch<?> patch = entry.getKey();
            long lastActive = entry.getValue();

            // 如果实体已死亡或长时间不活跃，清理
            if (patch.getOriginal() == null || !patch.getOriginal().isAlive()
                    || (currentGameTime - lastActive) > maxAgeTicks * 2L) {
                WeaponAfterimageHistory history = entityHistories.remove(patch);
                if (history != null) history.clear();
                it.remove();
            }
        }
    }

    /**
     * 获取指定实体的快照历史记录。
     * 返回按时间排序（最旧→最新）的快照列表。
     * 会自动过滤掉过期的快照。
     */
    public WeaponAfterimageHistory getHistory(LivingEntityPatch<?> entityPatch) {
        return entityHistories.get(entityPatch);
    }

    /**
     * 返回所有活跃实体的快照历史记录的不可变快照。
     */
    public Map<LivingEntityPatch<?>, WeaponAfterimageHistory> getAllHistories() {
        return entityHistories;
    }

    /**
     * 在世界卸载时清理所有数据。
     */
    public void onWorldUnload() {
        for (WeaponAfterimageHistory history : entityHistories.values()) {
            history.clear();
        }
        entityHistories.clear();
        pendingRegistration.clear();
        lastActiveTime.clear();
        clearRenderOverride();
    }

    /**
     * 当实体被移除时清理其数据。
     */
    public void onEntityRemoved(LivingEntityPatch<?> patch) {
        WeaponAfterimageHistory history = entityHistories.remove(patch);
        if (history != null) history.clear();
        lastActiveTime.remove(patch);
        pendingRegistration.remove(patch);
    }
}
