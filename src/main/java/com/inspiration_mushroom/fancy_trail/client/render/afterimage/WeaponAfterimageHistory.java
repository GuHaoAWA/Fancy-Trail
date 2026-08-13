package com.inspiration_mushroom.fancy_trail.client.render.afterimage;

import java.util.ArrayList;
import java.util.List;

public class WeaponAfterimageHistory {
    private final WeaponTransformSnapshot[] buffer;
    private int writeIndex;
    private int size;
    private final int capacity;

    public WeaponAfterimageHistory(int capacity) {
        this.capacity = Math.max(1, capacity);
        this.buffer = new WeaponTransformSnapshot[this.capacity];
        this.writeIndex = 0;
        this.size = 0;
    }

    public void push(WeaponTransformSnapshot snapshot) {
        buffer[writeIndex] = snapshot;
        writeIndex = (writeIndex + 1) % capacity;
        if (size < capacity) {
            size++;
        }
    }

    /** Oldest first. */
    public List<WeaponTransformSnapshot> getSnapshots() {
        List<WeaponTransformSnapshot> result = new ArrayList<>(size);
        if (size == 0) return result;

        // once full, writeIndex points at the slot about to be overwritten, i.e. the oldest entry
        int startIndex = size < capacity ? 0 : writeIndex;

        for (int i = 0; i < size; i++) {
            int idx = (startIndex + i) % capacity;
            WeaponTransformSnapshot snapshot = buffer[idx];
            if (snapshot != null) {
                result.add(snapshot);
            }
        }
        return result;
    }

    public WeaponTransformSnapshot getLatest() {
        if (size == 0) return null;
        return buffer[(writeIndex - 1 + capacity) % capacity];
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            buffer[i] = null;
        }
        writeIndex = 0;
        size = 0;
    }
}
