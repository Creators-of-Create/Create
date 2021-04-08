package com.simibubi.create.foundation.render.backend.gl.versioned;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * An instance of this class stores information
 * about what OpenGL features are available.
 *
 * Each field stores an enum variant that provides access to the
 * most appropriate version of a feature for the current system.
 */
public class GlCompat {
    public final MapBuffer mapBuffer;

    public final VertexArrayObject vertexArrayObject;
    public final InstancedArrays instancedArrays;
    public final DrawInstanced drawInstanced;

    public final RGPixelFormat pixelFormat;

    public GlCompat(GLCapabilities caps) {
        mapBuffer = getLatest(MapBuffer.class, caps);

        vertexArrayObject = getLatest(VertexArrayObject.class, caps);
        instancedArrays = getLatest(InstancedArrays.class, caps);
        drawInstanced = getLatest(DrawInstanced.class, caps);

        pixelFormat = getLatest(RGPixelFormat.class, caps);
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

    /**
     * Copied from:
     * <br> https://github.com/grondag/canvas/commit/820bf754092ccaf8d0c169620c2ff575722d7d96
     *
     * <p>Identical in function to {@link GL20C#glShaderSource(int, CharSequence)} but
     * passes a null pointer for string length to force the driver to rely on the null
     * terminator for string length.  This is a workaround for an apparent flaw with some
     * AMD drivers that don't receive or interpret the length correctly, resulting in
     * an access violation when the driver tries to read past the string memory.
     *
     * <p>Hat tip to fewizz for the find and the fix.
     */
    public static void safeShaderSource(int glId, CharSequence source) {
        final MemoryStack stack = MemoryStack.stackGet();
        final int stackPointer = stack.getPointer();

        try {
            final ByteBuffer sourceBuffer = MemoryUtil.memUTF8(source, true);
            final PointerBuffer pointers = stack.mallocPointer(1);
            pointers.put(sourceBuffer);

            GL20C.nglShaderSource(glId, 1, pointers.address0(), 0);
            org.lwjgl.system.APIUtil.apiArrayFree(pointers.address0(), 1);
        } finally {
            stack.setPointer(stackPointer);
        }
    }
}

