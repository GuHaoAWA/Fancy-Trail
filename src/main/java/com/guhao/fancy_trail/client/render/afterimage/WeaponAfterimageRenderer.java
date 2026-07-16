package com.guhao.fancy_trail.client.render.afterimage;

import com.guhao.fancy_trail.FT;
import com.guhao.fancy_trail.FTClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 3A 级武器残影 — 使用 Epic Fight SkinnedMesh。
 * 核心思路：通过 ThreadLocal 注入历史 modelMatrix，
 * 让 renderItemInHand 认为实体在历史位置，搭配历史 Pose 的关节矩阵，
 * 武器模型被冻结在挥动轨迹上。
 */
@OnlyIn(Dist.CLIENT)
public class WeaponAfterimageRenderer {

    private static final double MAX_RENDER_DISTANCE_SQ = 32.0 * 32.0;
    private static boolean loggedOnce = false;

    public static void renderAllGhosts(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Camera camera,
            float partialTick) {

        if (!FTClientConfig.getWeaponAfterimageEnabled()) return;

        WeaponAfterimageManager manager = WeaponAfterimageManager.getInstance();
        Map<LivingEntityPatch<?>, WeaponAfterimageHistory> histories = manager.getAllHistories();

        if (!loggedOnce) {
            FT.LOGGER.info("[WeaponAfterimage] renderAllGhosts, size={}", histories.size());
            loggedOnce = true;
        }
        if (histories.isEmpty()) return;

        List<Map.Entry<LivingEntityPatch<?>, WeaponAfterimageHistory>> entries =
                new ArrayList<>(histories.entrySet());

        long now = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
        Vec3 camPos = camera.getPosition();

        for (Map.Entry<LivingEntityPatch<?>, WeaponAfterimageHistory> entry : entries) {
            LivingEntityPatch<?> patch = entry.getKey();
            WeaponAfterimageHistory hist = entry.getValue();
            if (hist == null || hist.size() == 0) continue;

            LivingEntity entity = (LivingEntity) patch.getOriginal();
            if (entity == null || !entity.isAlive()) continue;
            if (camPos.distanceToSqr(entity.getPosition(partialTick)) > MAX_RENDER_DISTANCE_SQ) continue;

            List<WeaponTransformSnapshot> snaps = hist.getSnapshots();
            if (snaps.size() < 2) continue;

            int maxAge = FTClientConfig.getWeaponAfterimageMaxAgeTicks();
            float maxAlpha = FTClientConfig.getWeaponAfterimageMaxAlpha();
            float fadeExp = FTClientConfig.getWeaponAfterimageFadeExponent();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);

            for (int i = 0; i < snaps.size() - 1; i++) {
                WeaponTransformSnapshot snap = snaps.get(i);

                long age = snap.getAgeTicks(now);
                if (age > maxAge || age <= 0) continue;

                float t = (float) age / maxAge;
                float alpha = maxAlpha * (float) Math.pow(1.0 - Math.min(t, 1.0), fadeExp);
                if (alpha < 0.005f) continue;

                ItemStack stack = snap.weaponStack;
                if (stack.isEmpty()) continue;

                RenderItemBase renderer = ClientEngine.getInstance()
                        .renderEngine.getItemRenderer(stack);
                if (renderer == null) continue;

                // 历史 Pose → 关节矩阵
                OpenMatrix4f[] jointMatrices = patch.getArmature()
                        .getPoseAsTransformMatrix(snap.animationPose, false);

                // 历史 modelMatrix（仅旋转，位置由 PoseStack 处理）
                OpenMatrix4f histModel = OpenMatrix4f.createTranslation(0, 0, 0)
                        .rotateDeg(180.0F - snap.entityYRot, Vec3f.Y_AXIS);

                // ThreadLocal 注入 modelMatrix → LivingEntityPatchModelMatrixMixin
                WeaponAfterimageManager.setRenderOverride(histModel, snap.animationPose);

                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

                poseStack.pushPose();
                // 历史实体位置（相机相对） — Epic Fight 的 renderItemInHand 从 PoseStack 读位置
                Vec3 p = snap.entityPosition;
                poseStack.translate(
                        p.x - camPos.x,
                        p.y - camPos.y,
                        p.z - camPos.z);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - snap.entityYRot));

                try {
                    renderer.renderItemInHand(
                            stack, patch, snap.hand,
                            jointMatrices, bufferSource, poseStack,
                            0xF000F0, partialTick);
                } catch (Exception e) {
                    FT.LOGGER.error("[WeaponAfterimage] renderItemInHand failed", e);
                }
                poseStack.popPose();
            }

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            WeaponAfterimageManager.clearRenderOverride();
        }
    }
}
