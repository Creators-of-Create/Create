package com.simibubi.create.foundation.render.backend.gl.versioned;

import org.lwjgl.opengl.*;

public enum RGPixelFormat implements GlVersioned {
    GL30_RG {
        @Override
        public boolean supported(GLCapabilities caps) {
            return caps.OpenGL30;
        }

        @Override
        public int internalFormat() {
            return GL30.GL_RG8;
        }

        @Override
        public int format() {
            return GL30.GL_RG;
        }

        @Override
        public int byteCount() {
            return 2;
        }
    },
    GL11_RGB {
        @Override
        public boolean supported(GLCapabilities caps) {
            return caps.OpenGL11;
        }

        @Override
        public int internalFormat() {
            return GL11.GL_RGB8;
        }

        @Override
        public int format() {
            return GL11.GL_RGB;
        }

        @Override
        public int byteCount() {
            return 3;
        }
    },
    UNSUPPORTED {
        @Override
        public boolean supported(GLCapabilities caps) {
            return true;
        }

        @Override
        public int internalFormat() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int format() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int byteCount() {
            throw new UnsupportedOperationException();
        }
    }

    ;

    public abstract int internalFormat();
    public abstract int format();
    public abstract int byteCount();
}
