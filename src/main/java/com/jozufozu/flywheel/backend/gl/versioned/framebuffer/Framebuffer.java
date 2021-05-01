package com.jozufozu.flywheel.backend.gl.versioned.framebuffer;

import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GLCapabilities;

import com.jozufozu.flywheel.backend.gl.versioned.GlVersioned;

public enum Framebuffer implements GlVersioned {
	CORE {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.OpenGL30;
		}

		@Override
		public void bindFramebuffer(int target, int framebuffer) {
			GL30C.glBindFramebuffer(target, framebuffer);
		}
	},
	ARB {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_ARB_framebuffer_object;
		}

		@Override
		public void bindFramebuffer(int target, int framebuffer) {
			ARBFramebufferObject.glBindFramebuffer(target, framebuffer);
		}
	},
	EXT {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_EXT_framebuffer_object;
		}

		@Override
		public void bindFramebuffer(int target, int framebuffer) {
			EXTFramebufferObject.glBindFramebufferEXT(target, framebuffer);
		}
	},
	UNSUPPORTED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return true;
		}

		@Override
		public void bindFramebuffer(int target, int framebuffer) {
			throw new UnsupportedOperationException("Framebuffers not supported");
		}
	};

	public abstract void bindFramebuffer(int target, int framebuffer);
}
