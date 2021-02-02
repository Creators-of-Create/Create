package com.simibubi.create.foundation.render.gl.backend;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class Backend {
    private static final MapBuffer MAP_BUFFER = MapBuffer.GL30_RANGE;

    private Backend() {
        throw new UnsupportedOperationException();
    }

    public static void mapBuffer(int target, int offset, int length, Consumer<ByteBuffer> upload) {
        MAP_BUFFER.mapBuffer(target, offset, length, upload);
    }

    public static void mapBuffer(int target, int size, Consumer<ByteBuffer> upload) {
        MAP_BUFFER.mapBuffer(target, 0, size, upload);
    }
}
