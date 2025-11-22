package com.guhao.fancy_trail.unit;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientParticleDelayerUnit {
    private static final List<DelayedParticle> DELAYED_PARTICLES = new ArrayList<>();
    private static final List<DelayedParticle> TO_ADD = new ArrayList<>();
    private static boolean inTick = false;

    /**
     * 延迟生成粒子
     */
    public static void scheduleParticle(ClientLevel level, int eid, int animid, int jointId, int idx,
                                        long delayMs, SimpleParticleType particleType) {
        DelayedParticle particle = new DelayedParticle(level, eid, animid, jointId, idx,
                System.currentTimeMillis() + delayMs, particleType);

        if (inTick) {
            TO_ADD.add(particle);
        } else {
            DELAYED_PARTICLES.add(particle);
        }
    }

    /**
     * 在渲染刻中处理延迟粒子
     */
    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.RenderTickEvent.Phase.END) return;
        if (DELAYED_PARTICLES.isEmpty() && TO_ADD.isEmpty()) return;

        inTick = true;
        long currentTime = System.currentTimeMillis();

        // 处理到期的粒子
        Iterator<DelayedParticle> iterator = DELAYED_PARTICLES.iterator();
        while (iterator.hasNext()) {
            DelayedParticle particle = iterator.next();

            if (currentTime >= particle.scheduledTime) {
                particle.execute(event.renderTickTime); // 传递 partialTick 时间
                iterator.remove();
            }
        }

        // 添加新调度的粒子
        if (!TO_ADD.isEmpty()) {
            DELAYED_PARTICLES.addAll(TO_ADD);
            TO_ADD.clear();
        }

        inTick = false;
    }

    /**
     * 取消所有延迟粒子（用于级别卸载等情况）
     */
    public static void clearAll() {
        DELAYED_PARTICLES.clear();
        TO_ADD.clear();
    }

    /**
     * 延迟粒子任务
     */
    private static class DelayedParticle {
        private final ClientLevel level;
        private final int eid, animid, jointId, idx;
        private final long scheduledTime;
        private final SimpleParticleType particleType;
        private boolean executed = false;

        public DelayedParticle(ClientLevel level, int eid, int animid, int jointId, int idx,
                               long scheduledTime, SimpleParticleType particleType) {
            this.level = level;
            this.eid = eid;
            this.animid = animid;
            this.jointId = jointId;
            this.idx = idx;
            this.scheduledTime = scheduledTime;
            this.particleType = particleType;
        }

        public void execute(float partialTick) {
            if (executed) return;

            Entity entity = level.getEntity(eid);
            if (entity != null && entity.isAlive()) {

                level.addParticle(particleType,
                        Double.longBitsToDouble(eid),
                        0,
                        Double.longBitsToDouble(animid),
                        Double.longBitsToDouble(jointId),
                        Double.longBitsToDouble(idx),
                        0);
            }
            executed = true;
        }
    }
}
