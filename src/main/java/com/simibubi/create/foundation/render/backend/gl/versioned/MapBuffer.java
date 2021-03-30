package com.simibubi.create.foundation.render.backend.gl.versioned;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.lwjgl.opengl.ARBMapBufferRange;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

public enum MapBuffer implements GlVersioned {

    GL30_RANGE {
        @Override
        public boolean supported(GLCapabilities caps) {
            return caps.OpenGL30;
        }

        @Override
        public void mapBuffer(int target, int offset, int length, Consumer<ByteBuffer> upload) {
            ByteBuffer buffer = GL30.glMapBufferRange(target, offset, length, GL30.GL_MAP_WRITE_BIT);

            upload.accept(buffer);
            buffer.rewind();

            GL30.glUnmapBuffer(target);
        }
    },
    ARB_RANGE {
        @Override
        public boolean supported(GLCapabilities caps) {
            return caps.GL_ARB_map_buffer_range;
        }

        @Override
        public void mapBuffer(int target, int offset, int length, Consumer<ByteBuffer> upload) {
            ByteBuffer buffer = ARBMapBufferRange.glMapBufferRange(target, offset, length, GL30.GL_MAP_WRITE_BIT);

            upload.accept(buffer);
            buffer.rewind();

            GL30.glUnmapBuffer(target);
        }
    },
    GL15_MAP {
        @Override
        public boolean supported(GLCapabilities caps) {
            return caps.OpenGL15;
        }

        @Override
        public void mapBuffer(int target, int offset, int length, Consumer<ByteBuffer> upload) {
            ByteBuffer buffer = GL15.glMapBuffer(target, GL15.GL_WRITE_ONLY);

            buffer.position(offset);
            upload.accept(buffer);
            buffer.rewind();
            GL15.glUnmapBuffer(target);
        }
    },
    UNSUPPORTED {
        @Override
        public boolean supported(GLCapabilities caps) {
            return true;
        }

        @Override
        public void mapBuffer(int target, int offset, int length, Consumer<ByteBuffer> upload) {
            throw new UnsupportedOperationException("glMapBuffer not supported");
        }
    };


    public abstract void mapBuffer(int target, int offset, int length, Consumer<ByteBuffer> upload);
}
