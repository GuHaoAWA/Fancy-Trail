package com.guhao.fancy_trail.unit.Function;


import java.util.Objects;

@FunctionalInterface
public interface ScreenResizeEventHandler {
    void consume(float w, float h);

    default ScreenResizeEventHandler andThen(ScreenResizeEventHandler after) {
        Objects.requireNonNull(after);
        return (w, h) -> {
            consume(w, h);
            after.consume(w, h);
        };
    }
}
