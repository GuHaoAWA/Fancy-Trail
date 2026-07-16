package com.guhao.fancy_trail.client.render.afterimage;

import java.util.ArrayList;
import java.util.List;

/**
 * 固定容量的环形缓冲区，存储武器的变换历史。
 * 当缓冲区满时，新的快照覆盖最旧的快照。
 */
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

    /**
     * 添加新快照。如果缓冲区满，覆盖最旧的。
     */
    public void push(WeaponTransformSnapshot snapshot) {
        buffer[writeIndex] = snapshot;
        writeIndex = (writeIndex + 1) % capacity;
        if (size < capacity) {
            size++;
        }
    }

    /**
     * 返回所有非空快照的列表，按从最旧到最新的顺序排列。
     * 第一个元素的 `gameTime` 最小（最旧），最后一个最大（最新）。
     */
    public List<WeaponTransformSnapshot> getSnapshots() {
        List<WeaponTransformSnapshot> result = new ArrayList<>(size);
        if (size == 0) return result;

        int startIndex;
        if (size < capacity) {
            // 缓冲区尚未填满，从索引 0 开始
            startIndex = 0;
        } else {
            // 缓冲区已满，最旧的条目在 writeIndex（即将被覆盖的位置）
            startIndex = writeIndex;
        }

        for (int i = 0; i < size; i++) {
            int idx = (startIndex + i) % capacity;
            WeaponTransformSnapshot snapshot = buffer[idx];
            if (snapshot != null) {
                result.add(snapshot);
            }
        }
        return result;
    }

    /**
     * 获取最新快照（如果有）
     */
    public WeaponTransformSnapshot getLatest() {
        if (size == 0) return null;
        int latestIdx = (writeIndex - 1 + capacity) % capacity;
        return buffer[latestIdx];
    }

    /**
     * 当前缓冲区中的快照数量
     */
    public int size() {
        return size;
    }

    /**
     * 清空缓冲区
     */
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            buffer[i] = null;
        }
        writeIndex = 0;
        size = 0;
    }
}
