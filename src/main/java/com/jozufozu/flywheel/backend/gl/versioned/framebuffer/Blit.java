package com.jozufozu.flywheel.backend.gl.versioned.framebuffer;

import org.lwjgl.opengl.EXTFramebufferBlit;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

import com.jozufozu.flywheel.backend.gl.versioned.GlVersioned;

public enum Blit implements GlVersioned {
	CORE {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.OpenGL30;
		}

		@Override
		public void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
			GL30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
		}
	},
	EXT {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_EXT_framebuffer_blit;
		}

		@Override
		public void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
			EXTFramebufferBlit.glBlitFramebufferEXT(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
		}
	},
	UNSUPPORTED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return true;
		}

		@Override
		public void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
			throw new UnsupportedOperationException("Framebuffer blitting not supported.");
		}
	};

	public abstract void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter);
}
