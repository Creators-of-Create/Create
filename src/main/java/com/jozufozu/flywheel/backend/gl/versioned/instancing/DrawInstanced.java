package com.jozufozu.flywheel.backend.gl.versioned.instancing;

import org.lwjgl.opengl.ARBDrawInstanced;
import org.lwjgl.opengl.EXTDrawInstanced;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.versioned.GlVersioned;

public enum DrawInstanced implements GlVersioned {
	GL31_DRAW_INSTANCED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.OpenGL31;
		}

		@Override
		public void drawArraysInstanced(GlPrimitive mode, int first, int count, int primcount) {
			GL31.glDrawArraysInstanced(mode.glEnum, first, count, primcount);
		}

		@Override
		public void drawElementsInstanced(GlPrimitive mode, int elementCount, GlNumericType type, long indices, int primcount) {
			GL31.glDrawElementsInstanced(mode.glEnum, elementCount, type.getGlEnum(), indices, primcount);
		}
	},
	ARB_DRAW_INSTANCED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_ARB_draw_instanced;
		}

		@Override
		public void drawArraysInstanced(GlPrimitive mode, int first, int count, int primcount) {
			ARBDrawInstanced.glDrawArraysInstancedARB(mode.glEnum, first, count, primcount);
		}

		@Override
		public void drawElementsInstanced(GlPrimitive mode, int elementCount, GlNumericType type, long indices, int primcount) {
			ARBDrawInstanced.glDrawElementsInstancedARB(mode.glEnum, elementCount, type.getGlEnum(), indices, primcount);
		}
	},
	EXT_DRAW_INSTANCED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_EXT_draw_instanced;
		}

		@Override
		public void drawArraysInstanced(GlPrimitive mode, int first, int count, int primcount) {
			EXTDrawInstanced.glDrawArraysInstancedEXT(mode.glEnum, first, count, primcount);
		}

		@Override
		public void drawElementsInstanced(GlPrimitive mode, int elementCount, GlNumericType type, long indices, int primcount) {
			EXTDrawInstanced.glDrawElementsInstancedEXT(mode.glEnum, elementCount, type.getGlEnum(), indices, primcount);
		}
	},
	UNSUPPORTED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return true;
		}
	};


	public void drawArraysInstanced(GlPrimitive mode, int first, int count, int primcount) {
		throw new UnsupportedOperationException();
	}

	public void drawElementsInstanced(GlPrimitive mode, int elementCount, GlNumericType type, long indices, int primcount) {
		throw new UnsupportedOperationException();
	}
}
