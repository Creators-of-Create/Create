package com.simibubi.create.foundation.render.backend.gl.versioned.instancing;

import org.lwjgl.opengl.ARBDrawInstanced;
import org.lwjgl.opengl.EXTDrawInstanced;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;

import com.simibubi.create.foundation.render.backend.gl.versioned.GlVersioned;

public enum DrawInstanced implements GlVersioned {
	GL31_DRAW_INSTANCED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.OpenGL31;
		}

		@Override
		public void drawArraysInstanced(int mode, int first, int count, int primcount) {
			GL31.glDrawArraysInstanced(mode, first, count, primcount);
		}
	},
	ARB_DRAW_INSTANCED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_ARB_draw_instanced;
		}

		@Override
		public void drawArraysInstanced(int mode, int first, int count, int primcount) {
			ARBDrawInstanced.glDrawArraysInstancedARB(mode, first, count, primcount);
		}
	},
	EXT_DRAW_INSTANCED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_EXT_draw_instanced;
		}

		@Override
		public void drawArraysInstanced(int mode, int first, int count, int primcount) {
			EXTDrawInstanced.glDrawArraysInstancedEXT(mode, first, count, primcount);
		}
	},
	UNSUPPORTED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return true;
		}

		@Override
		public void drawArraysInstanced(int mode, int first, int count, int primcount) {
			throw new UnsupportedOperationException();
		}
	};

	public abstract void drawArraysInstanced(int mode, int first, int count, int primcount);
}
