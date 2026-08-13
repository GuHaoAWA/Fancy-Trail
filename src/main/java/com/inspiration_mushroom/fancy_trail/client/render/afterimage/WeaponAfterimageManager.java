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
import java.util.concurrent.ConcurrentHashMap;

public class WeaponAfterimageManager {

    private static final WeaponAfterimageManager INSTANCE = new WeaponAfterimageManager();

    public static WeaponAfterimageManager getInstance() {
        return INSTANCE;
    }

    private WeaponAfterimageManager() {}

    // set around one ghost draw so LivingEntityPatchModelMatrixMixin hands EF the historic pose
    // instead of the live one; every set must be paired with clearRenderOverride()
    public static final ThreadLocal<OpenMatrix4f> RENDER_OVERRIDE_MATRIX = new ThreadLocal<>();
    public static final ThreadLocal<Pose> RENDER_OVERRIDE_POSE = new ThreadLocal<>();

    public static void setRenderOverride(OpenMatrix4f modelMatrix, Pose pose) {
        RENDER_OVERRIDE_MATRIX.set(modelMatrix);
        RENDER_OVERRIDE_POSE.set(pose);
    }

    public static void clearRenderOverride() {
        RENDER_OVERRIDE_MATRIX.remove();
        RENDER_OVERRIDE_POSE.remove();
    }

    private final Map<LivingEntityPatch<?>, WeaponAfterimageHistory> entityHistories = new ConcurrentHashMap<>();
    private final Map<LivingEntityPatch<?>, Long> lastActiveTime = new ConcurrentHashMap<>();

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
            history = new WeaponAfterimageHistory(FTClientConfig.getWeaponAfterimageGhostCount() + 2);
            entityHistories.put(entityPatch, history);
        }

        history.push(new WeaponTransformSnapshot(
                jointLocalTransform,
                animationPose,
                entityPosition,
                entityYRot,
                weaponStack,
                hand,
                jointId,
                gameTime
        ));
        lastActiveTime.put(entityPatch, gameTime);
    }

    public void update(long currentGameTime) {
        int maxAgeTicks = FTClientConfig.getWeaponAfterimageMaxAgeTicks();
        Iterator<Map.Entry<LivingEntityPatch<?>, Long>> it = lastActiveTime.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<LivingEntityPatch<?>, Long> entry = it.next();
            LivingEntityPatch<?> patch = entry.getKey();

            if (patch.getOriginal() == null || !patch.getOriginal().isAlive()
                    || (currentGameTime - entry.getValue()) > maxAgeTicks * 2L) {
                WeaponAfterimageHistory history = entityHistories.remove(patch);
                if (history != null) history.clear();
                it.remove();
            }
        }
    }

    public WeaponAfterimageHistory getHistory(LivingEntityPatch<?> entityPatch) {
        return entityHistories.get(entityPatch);
    }

    public Map<LivingEntityPatch<?>, WeaponAfterimageHistory> getAllHistories() {
        return entityHistories;
    }

    public void onWorldUnload() {
        for (WeaponAfterimageHistory history : entityHistories.values()) {
            history.clear();
        }
        entityHistories.clear();
        lastActiveTime.clear();
        clearRenderOverride();
    }

    public void onEntityRemoved(LivingEntityPatch<?> patch) {
        WeaponAfterimageHistory history = entityHistories.remove(patch);
        if (history != null) history.clear();
        lastActiveTime.remove(patch);
    }
}
