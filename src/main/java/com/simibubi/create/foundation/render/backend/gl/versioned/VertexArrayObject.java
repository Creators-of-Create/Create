package com.simibubi.create.foundation.render.backend.gl.versioned;

import org.lwjgl.opengl.*;

public enum VertexArrayObject implements GlVersioned {
    GL30_VAO {
        @Override
        public boolean supported(GLCapabilities caps) {
            return caps.OpenGL30;
        }

        @Override
        public int genVertexArrays() {
            return GL30.glGenVertexArrays();
        }

        @Override
        public void bindVertexArray(int array) {
            GL30.glBindVertexArray(array);
        }

        @Override
        public void deleteVertexArrays(int array) {
            GL30.glDeleteVertexArrays(array);
        }
    },
    ARB_VAO {
        @Override
        public boolean supported(GLCapabilities caps) {
            return caps.GL_ARB_vertex_array_object;
        }

        @Override
        public int genVertexArrays() {
            return ARBVertexArrayObject.glGenVertexArrays();
        }

        @Override
        public void bindVertexArray(int array) {
            ARBVertexArrayObject.glBindVertexArray(array);
        }

        @Override
        public void deleteVertexArrays(int array) {
            ARBVertexArrayObject.glDeleteVertexArrays(array);
        }
    },
    UNSUPPORTED {
        @Override
        public boolean supported(GLCapabilities caps) {
            return true;
        }

        @Override
        public int genVertexArrays() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void bindVertexArray(int array) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteVertexArrays(int array) {
            throw new UnsupportedOperationException();
        }
    }

    ;

    public abstract int genVertexArrays();

    public abstract void bindVertexArray(int array);

    public abstract void deleteVertexArrays(int array);
}
