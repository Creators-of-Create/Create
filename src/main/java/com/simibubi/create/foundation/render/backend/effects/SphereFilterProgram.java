package com.simibubi.create.foundation.render.backend.effects;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;

import com.simibubi.create.foundation.render.backend.RenderUtil;
import com.simibubi.create.foundation.render.backend.gl.GlBuffer;
import com.simibubi.create.foundation.render.backend.gl.shader.GlProgram;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class SphereFilterProgram extends GlProgram {

	protected static final int UBO_BINDING = 4;

	protected static final int SPHERE_FILTER_SIZE = 24 * 4; // <vec4, float + padding, mat4>
	protected static final int MAX_FILTERS = 256; // arbitrary

	protected static final int EXTRA_INFO = 16; // array length: int + padding
	protected static final int ALL_FILTERS_SIZE = MAX_FILTERS * SPHERE_FILTER_SIZE;

	protected static final int BUFFER_SIZE = EXTRA_INFO + ALL_FILTERS_SIZE;

	public final GlBuffer effectsUBO;

	protected final ArrayList<FilterSphere> filters = new ArrayList<>(16);

	protected final int uniformBlock;

	protected final int uDepth;
	protected final int uColor;

	protected final int uInverseProjection;
	protected final int uInverseView;

	protected final int uNearPlane;
	protected final int uFarPlane;

	protected final int uCameraPos;

	public SphereFilterProgram(ResourceLocation name, int handle) {
		super(name, handle);

		effectsUBO = new GlBuffer(GL31.GL_UNIFORM_BUFFER);

		uniformBlock = GL31.glGetUniformBlockIndex(handle, "Filters");

		GL31.glUniformBlockBinding(handle, uniformBlock, UBO_BINDING);

		effectsUBO.bind();
		effectsUBO.alloc(BUFFER_SIZE, GL20.GL_STATIC_DRAW);
		GL31.glBindBufferBase(effectsUBO.getBufferType(), UBO_BINDING, effectsUBO.handle());
		effectsUBO.unbind();

		uInverseProjection = getUniformLocation("uInverseProjection");
		uInverseView = getUniformLocation("uInverseView");
		uNearPlane = getUniformLocation("uNearPlane");
		uFarPlane = getUniformLocation("uFarPlane");
		uCameraPos = getUniformLocation("uCameraPos");

		bind();
		uDepth = setSamplerBinding("uDepth", 8);
		uColor = setSamplerBinding("uColor", 9);
		unbind();
	}

	public void setNearPlane(float nearPlane) {
		GL20.glUniform1f(uNearPlane, nearPlane);
	}

	public void setFarPlane(float farPlane) {
		GL20.glUniform1f(uFarPlane, farPlane);
	}

	public void setCameraPos(Vector3d pos) {
		GL20.glUniform3f(uCameraPos, (float) pos.x, (float) pos.y, (float) pos.z);
	}

	public void clear() {
		filters.clear();
	}

	public void addSphere(FilterSphere filterSphere) {
		filters.add(filterSphere);
	}

	public void uploadFilters() {
		effectsUBO.bind(GL20.GL_ARRAY_BUFFER);
		effectsUBO.map(GL20.GL_ARRAY_BUFFER, 0, BUFFER_SIZE, this::uploadUBO);
		effectsUBO.unbind(GL20.GL_ARRAY_BUFFER);
	}

	public void bindInverseProjection(Matrix4f mat) {
		uploadMatrixUniform(uInverseProjection, mat);
	}

	public void bindInverseView(Matrix4f mat) {
		uploadMatrixUniform(uInverseView, mat);
	}

	public void bindDepthTexture(int textureObject) {
		GL20.glActiveTexture(GL20.GL_TEXTURE8);
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, textureObject);
	}

	public void bindColorTexture(int textureObject) {
		GL20.glActiveTexture(GL20.GL_TEXTURE9);
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, textureObject);
	}

	private void uploadUBO(ByteBuffer buf) {
		buf.putInt(filters.size());
		buf.position(16);
		FloatBuffer floatBuffer = buf.asFloatBuffer();

		filters.forEach(it -> it.write(floatBuffer));
	}

	public static class FilterSphere {
		public Vector3d center;
		public float radius;
		public float feather;
		public float fade;
		public float strength = 1;
		public boolean hsv;

		public Matrix4f filter;

		public FilterSphere setCenter(Vector3d center) {
			this.center = center;
			return this;
		}

		public FilterSphere setRadius(float radius) {
			this.radius = radius;
			return this;
		}

		public FilterSphere setFeather(float feather) {
			this.feather = feather;
			return this;
		}

		public FilterSphere setFade(float fade) {
			this.fade = fade;
			return this;
		}

		public FilterSphere setStrength(float strength) {
			this.strength = strength;
			return this;
		}

		public FilterSphere setHsv(boolean hsv) {
			this.hsv = hsv;
			return this;
		}

		public FilterSphere setFilter(Matrix4f filter) {
			this.filter = filter;
			return this;
		}

		public void write(FloatBuffer buf) {
			buf.put(new float[]{
					(float) center.x,
					(float) center.y,
					(float) center.z,
					radius,
					feather,
					fade,
					strength,
					hsv ? 1f : 0f,
			});

			buf.put(RenderUtil.writeMatrix(filter));
		}
	}
}
