package com.simibubi.create.foundation.render.backend.gl.versioned.instancing;

import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GLCapabilities;

import com.simibubi.create.foundation.render.backend.gl.versioned.GlVersioned;

public enum InstancedArrays implements GlVersioned {
	GL33_INSTANCED_ARRAYS {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.OpenGL33;
		}

		@Override
		public void vertexAttribDivisor(int index, int divisor) {
			GL33.glVertexAttribDivisor(index, divisor);
		}
	},
	ARB_INSTANCED_ARRAYS {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_ARB_instanced_arrays;
        }

        @Override
        public void vertexAttribDivisor(int index, int divisor) {
            ARBInstancedArrays.glVertexAttribDivisorARB(index, divisor);
        }
    },
    UNSUPPORTED {
        @Override
        public boolean supported(GLCapabilities caps) {
            return true;
        }

        @Override
        public void vertexAttribDivisor(int index, int divisor) {
            throw new UnsupportedOperationException();
        }
    }

    ;

    public abstract void vertexAttribDivisor(int index, int divisor);
}
