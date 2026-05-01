package com.guhao.fancy_trail.unit;

import com.guhao.fancy_trail.FTClientConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FTunit {
    public static void spawnAdditionalParticles(Level level, List<Vec3> startPositions, List<Vec3> endPositions) {
        for(int i = 0; i < startPositions.size(); ++i) {
            Vec3 startPos = (Vec3)startPositions.get(i);
            Vec3 endPos = (Vec3)endPositions.get(i);
            Vec3 direction = endPos.subtract(startPos).normalize();
            double speed = (double) FTClientConfig.getAdditionalParticleSpeed();

            for(int j = 0; j < 1; ++j) {
                Vec3 particlePos = startPos.add(direction.scale((double)j * 0.5));
                level.addParticle(FTClientConfig.getAdditionalParticleType(), true, particlePos.x, particlePos.y, particlePos.z, speed * direction.x, speed * direction.y, speed * direction.z);
            }
        }

    }
}
