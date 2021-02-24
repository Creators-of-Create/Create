package com.simibubi.create.foundation.render.backend.gl.versioned;

import org.lwjgl.opengl.GLCapabilities;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * An instance of this class stores information
 * about what OpenGL features are available.
 *
 * Each field stores an enum variant that provides access to the
 * most appropriate version of a feature for the current system.
 */
public class GlFunctions {
    public final MapBuffer mapBuffer;

    public final VertexArrayObject vertexArrayObject;
    public final InstancedArrays instancedArrays;
    public final DrawInstanced drawInstanced;

    public GlFunctions(GLCapabilities caps) {
        mapBuffer = getLatest(MapBuffer.class, caps);

        vertexArrayObject = getLatest(VertexArrayObject.class, caps);
        instancedArrays = getLatest(InstancedArrays.class, caps);
        drawInstanced = getLatest(DrawInstanced.class, caps);
    }

    public void mapBuffer(int target, int offset, int length, Consumer<ByteBuffer> upload) {
        mapBuffer.mapBuffer(target, offset, length, upload);
    }

    public void vertexAttribDivisor(int index, int divisor) {
        instancedArrays.vertexAttribDivisor(index, divisor);
    }

    public void drawArraysInstanced(int mode, int first, int count, int primcount) {
        drawInstanced.drawArraysInstanced(mode, first, count, primcount);
    }

    public int genVertexArrays() {
        return vertexArrayObject.genVertexArrays();
    }

    public void deleteVertexArrays(int array) {
        vertexArrayObject.deleteVertexArrays(array);
    }

    public void bindVertexArray(int array) {
        vertexArrayObject.bindVertexArray(array);
    }

    public boolean vertexArrayObjectsSupported() {
        return vertexArrayObject != VertexArrayObject.UNSUPPORTED;
    }

    public boolean instancedArraysSupported() {
        return instancedArrays != InstancedArrays.UNSUPPORTED;
    }

    public boolean drawInstancedSupported() {
        return drawInstanced != DrawInstanced.UNSUPPORTED;
    }

    /**
     * Get the most compatible version of a specific OpenGL feature by iterating over enum constants in order.
     *
     * @param clazz The class of the versioning enum.
     * @param caps The current system's supported features.
     * @param <V> The type of the versioning enum.
     * @return The first defined enum variant to return true.
     */
    public static <V extends Enum<V> & GlVersioned> V getLatest(Class<V> clazz, GLCapabilities caps) {
        V[] constants = clazz.getEnumConstants();
        V last = constants[constants.length - 1];
        if (!last.supported(caps)) {
            throw new IllegalStateException("");
        }

        return Arrays.stream(constants).filter(it -> it.supported(caps)).findFirst().get();
    }
}

