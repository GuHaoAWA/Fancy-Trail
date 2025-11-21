package com.guhao.fancy_trail.client.pipeline;

import net.minecraft.client.particle.Particle;

import java.util.PriorityQueue;
import java.util.Queue;

public class ParticleEngineHelper {

    public static PriorityQueue<PostParticles> createQueue() {
        return new PriorityQueue<>();
    }

    public record PostParticles(PostParticleRenderType rt,
                                Queue<Particle> particles) implements Comparable<PostParticles> {
        public static PostParticles of(PostParticleRenderType rt, Queue<Particle> particles) {
            return new PostParticles(rt, particles);
        }

        @Override
        public int compareTo(PostParticles other) {
            return Integer.compare(other.rt.priority, this.rt.priority);  // 降序排列
        }
    }
}
