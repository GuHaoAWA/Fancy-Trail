package com.inspiration_mushroom.fancy_trail.client.render.custom;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.client.animation.property.TrailInfo;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BloomSettings {
    public static final BloomSettings DEFAULT = new BloomSettings(255, 255, 255, 1.0f);

    private static final Map<ResourceLocation, BloomSettings> TEXTURE_SETTINGS = new ConcurrentHashMap<>();
    private static final ReferenceQueue<TrailInfo> TRAIL_INFO_QUEUE = new ReferenceQueue<>();
    private static final Map<IdentityWeakReference, BloomSettings> TRAIL_INFO_SETTINGS = new HashMap<>();

    public final float r;
    public final float g;
    public final float b;
    public final float intensity;

    public BloomSettings(int r, int g, int b, float intensity) {
        this.r = clampColor(r) / 255.0f;
        this.g = clampColor(g) / 255.0f;
        this.b = clampColor(b) / 255.0f;
        this.intensity = sanitizeIntensity(intensity);
    }

    public static BloomSettings fromJson(JsonObject bloom) {
        int r = 255;
        int g = 255;
        int b = 255;

        if (bloom.has("rgb") && bloom.get("rgb").isJsonArray()) {
            JsonArray rgb = bloom.getAsJsonArray("rgb");
            if (rgb.size() >= 3) {
                r = rgb.get(0).getAsInt();
                g = rgb.get(1).getAsInt();
                b = rgb.get(2).getAsInt();
            }
        }

        float intensity = bloom.has("intensity") ? bloom.get("intensity").getAsFloat() : 1.0f;
        return new BloomSettings(r, g, b, intensity);
    }

    public static void register(ResourceLocation texture, BloomSettings settings) {
        if (texture != null && settings != null) {
            TEXTURE_SETTINGS.put(texture, settings);
        }
    }

    public static void register(TrailInfo trailInfo, BloomSettings settings) {
        if (trailInfo == null || settings == null) {
            return;
        }

        synchronized (TRAIL_INFO_SETTINGS) {
            removeCollectedTrailInfos();
            TRAIL_INFO_SETTINGS.put(new IdentityWeakReference(trailInfo, TRAIL_INFO_QUEUE), settings);
        }
    }

    public static BloomSettings find(TrailInfo trailInfo) {
        if (trailInfo == null) {
            return null;
        }

        synchronized (TRAIL_INFO_SETTINGS) {
            removeCollectedTrailInfos();
            return TRAIL_INFO_SETTINGS.get(new IdentityWeakReference(trailInfo));
        }
    }

    public static BloomSettings get(TrailInfo trailInfo) {
        BloomSettings settings = find(trailInfo);
        return settings != null ? settings : get(trailInfo == null ? null : trailInfo.texturePath());
    }

    public static BloomSettings get(ResourceLocation texture) {
        return texture == null ? DEFAULT : TEXTURE_SETTINGS.getOrDefault(texture, DEFAULT);
    }

    public static float sanitizeIntensity(float intensity) {
        return Float.isFinite(intensity) ? intensity : DEFAULT.intensity;
    }

    public static int size() {
        synchronized (TRAIL_INFO_SETTINGS) {
            removeCollectedTrailInfos();
            return TEXTURE_SETTINGS.size() + TRAIL_INFO_SETTINGS.size();
        }
    }

    public static void clear() {
        TEXTURE_SETTINGS.clear();
        synchronized (TRAIL_INFO_SETTINGS) {
            TRAIL_INFO_SETTINGS.clear();
            while (TRAIL_INFO_QUEUE.poll() != null) {
            }
        }
    }

    private static int clampColor(int color) {
        return Math.max(0, Math.min(color, 255));
    }

    private static void removeCollectedTrailInfos() {
        IdentityWeakReference reference;
        while ((reference = (IdentityWeakReference) TRAIL_INFO_QUEUE.poll()) != null) {
            TRAIL_INFO_SETTINGS.remove(reference);
        }
    }

    private static final class IdentityWeakReference extends WeakReference<TrailInfo> {
        private final int identityHash;

        private IdentityWeakReference(TrailInfo trailInfo) {
            super(trailInfo);
            this.identityHash = System.identityHashCode(trailInfo);
        }

        private IdentityWeakReference(TrailInfo trailInfo, ReferenceQueue<TrailInfo> queue) {
            super(trailInfo, queue);
            this.identityHash = System.identityHashCode(trailInfo);
        }

        @Override
        public int hashCode() {
            return identityHash;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof IdentityWeakReference other)) {
                return false;
            }

            TrailInfo trailInfo = get();
            return trailInfo != null && trailInfo == other.get();
        }
    }
}
