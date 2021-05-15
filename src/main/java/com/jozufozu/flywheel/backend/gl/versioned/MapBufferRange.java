package com.jozufozu.flywheel.backend.gl.versioned;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.ARBMapBufferRange;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;

public enum MapBufferRange implements GlVersioned {

	GL30_RANGE {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.OpenGL30;
		}

		@Override
		public ByteBuffer mapBuffer(GlBufferType target, long offset, long length, int access) {
			return GL30.glMapBufferRange(target.glEnum, offset, length, access);
		}
	},
	ARB_RANGE {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_ARB_map_buffer_range;
		}

		@Override
		public ByteBuffer mapBuffer(GlBufferType target, long offset, long length, int access) {
			return ARBMapBufferRange.glMapBufferRange(target.glEnum, offset, length, access);
		}
	},
	UNSUPPORTED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return true;
		}

		@Override
		public ByteBuffer mapBuffer(GlBufferType target, long offset, long length, int access) {
			throw new UnsupportedOperationException("glMapBuffer not supported");
		}
	};


	public abstract ByteBuffer mapBuffer(GlBufferType target, long offset, long length, int access);
}
