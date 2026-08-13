package com.inspiration_mushroom.fancy_trail.unit;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientParticleDelayerUnit {
    private static final List<DelayedParticle> DELAYED_PARTICLES = new ArrayList<>();
    private static final List<DelayedParticle> TO_ADD = new ArrayList<>();
    private static boolean inTick = false;

    /**
     * Schedule a delayed particle spawn.
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
     * Process delayed particles on the render tick.
     * 1.21 NeoForge: upstream subscribed to TickEvent.RenderTickEvent and filtered on
     * Phase.END; RenderFrameEvent.Post IS the end-of-frame phase, so the phase check is gone.
     */
    public static void onRenderTick(RenderFrameEvent.Post event) {
        if (DELAYED_PARTICLES.isEmpty() && TO_ADD.isEmpty()) return;

        inTick = true;

        long currentTime = System.currentTimeMillis();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        // Execute due particles
        Iterator<DelayedParticle> iterator = DELAYED_PARTICLES.iterator();
        while (iterator.hasNext()) {
            DelayedParticle particle = iterator.next();

            if (currentTime >= particle.scheduledTime) {
                particle.execute(partialTick);
                iterator.remove();
            }
        }

        // Add newly scheduled particles
        if (!TO_ADD.isEmpty()) {
            DELAYED_PARTICLES.addAll(TO_ADD);
            TO_ADD.clear();
        }

        inTick = false;
    }

    /**
     * Cancel all delayed particles (level unload etc.).
     */
    public static void clearAll() {
        DELAYED_PARTICLES.clear();
        TO_ADD.clear();
    }

    /**
     * A delayed particle task.
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
