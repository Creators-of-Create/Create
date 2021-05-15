package com.jozufozu.flywheel.backend.gl.versioned;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.lwjgl.opengl.ARBMapBufferRange;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

import com.jozufozu.flywheel.backend.gl.GlBufferType;

public enum MapBuffer implements GlVersioned {

	GL30_RANGE {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.OpenGL30;
		}

		public ByteBuffer mapBuffer(GlBufferType target, long offset, long length, int access) {
			return GL30.glMapBufferRange(target.glEnum, offset, length, access);
		}

		@Override
		public void mapBuffer(GlBufferType target, int offset, int length, Consumer<ByteBuffer> upload) {
			ByteBuffer buffer = mapBuffer(target, offset, length, GL30.GL_MAP_WRITE_BIT);

			upload.accept(buffer);
			buffer.rewind();

			GL30.glUnmapBuffer(target.glEnum);
		}
	},
	ARB_RANGE {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_ARB_map_buffer_range;
		}

		public ByteBuffer mapBuffer(GlBufferType target, long offset, long length, int access) {
			return ARBMapBufferRange.glMapBufferRange(target.glEnum, offset, length, access);
		}

		@Override
		public void mapBuffer(GlBufferType target, int offset, int length, Consumer<ByteBuffer> upload) {
			ByteBuffer buffer = ARBMapBufferRange.glMapBufferRange(target.glEnum, offset, length, GL30.GL_MAP_WRITE_BIT);

			upload.accept(buffer);
			buffer.rewind();

			GL30.glUnmapBuffer(target.glEnum);
		}
	},
	GL15_MAP {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.OpenGL15;
		}

		@Override
		public void mapBuffer(GlBufferType target, int offset, int length, Consumer<ByteBuffer> upload) {
			ByteBuffer buffer = GL15.glMapBuffer(target.glEnum, GL15.GL_WRITE_ONLY);

			buffer.position(offset);
			upload.accept(buffer);
			buffer.rewind();
			GL15.glUnmapBuffer(target.glEnum);
		}
	},
	UNSUPPORTED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return true;
		}

		@Override
		public void mapBuffer(GlBufferType target, int offset, int length, Consumer<ByteBuffer> upload) {
			throw new UnsupportedOperationException("glMapBuffer not supported");
		}

		@Override
		public void unmapBuffer(int target) {
			throw new UnsupportedOperationException("glMapBuffer not supported");
		}
	};


	public void unmapBuffer(int target) {
		GL15.glUnmapBuffer(target);
	}

	public abstract void mapBuffer(GlBufferType target, int offset, int length, Consumer<ByteBuffer> upload);
}
