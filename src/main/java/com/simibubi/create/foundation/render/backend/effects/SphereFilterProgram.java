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

	protected static final int BLOCK_BINDING = 4;

	protected static final int SPHERE_FILTER_SIZE = 4 * 16 + 16 + 4 * 16 * 16;

	protected static final int MAX_FILTERS = 16;

	protected static final int BUFFER_SIZE = 4 + MAX_FILTERS * SPHERE_FILTER_SIZE;

	GlBuffer effectsUBO;

	protected final ArrayList<FilterSphere> filters = new ArrayList<>(16);

	protected final int uniformBlock;

	protected final int uDepth;
	protected final int uColor;

	protected final int uInverseProjection;
	protected final int uInverseView;

	protected final int uNearPlane;
	protected final int uFarPlane;

	protected final int uCameraPos;
	protected final int testParam;
//	protected final int uSphereCenter;
//	protected final int uSphereRadius;
//	protected final int uSphereFeather;
//	protected final int uColorFilter;

	public SphereFilterProgram(ResourceLocation name, int handle) {
		super(name, handle);

		effectsUBO = new GlBuffer(GL31.GL_UNIFORM_BUFFER);

		uniformBlock = GL31.glGetUniformBlockIndex(handle, "Filters");

		GL31.glUniformBlockBinding(handle, uniformBlock, BLOCK_BINDING);

		effectsUBO.bind();
		effectsUBO.alloc(BUFFER_SIZE, GL20.GL_STATIC_DRAW);
		GL31.glBindBufferBase(effectsUBO.getBufferType(), BLOCK_BINDING, effectsUBO.handle());
		effectsUBO.unbind();

		uInverseProjection = getUniformLocation("uInverseProjection");
		uInverseView = getUniformLocation("uInverseView");
		uNearPlane = getUniformLocation("uNearPlane");
		uFarPlane = getUniformLocation("uFarPlane");
		uCameraPos = getUniformLocation("uCameraPos");
		testParam = getUniformLocation("testParam");
//
//		uSphereCenter = getUniformLocation("uSphereCenter");
//		uSphereRadius = getUniformLocation("uSphereRadius");
//		uSphereFeather = getUniformLocation("uSphereFeather");
//		uColorFilter = getUniformLocation("uColorFilter");

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

	public void setTestParam(float farPlane) {
		GL20.glUniform1f(testParam, farPlane);
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

//	public void setSphere(FilterSphere sphere) {
//		GL20.glUniform3f(uSphereCenter, (float) sphere.center.x, (float) sphere.center.y, (float) sphere.center.z);
//
//		GL20.glUniform1f(uSphereRadius, sphere.radius);
//		GL20.glUniform1f(uSphereFeather, sphere.feather);
//
//		uploadMatrixUniform(uColorFilter, sphere.filter);
//	}

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

		//floatBuffer.position(4);
		filters.forEach(it -> it.write(floatBuffer));
	}

	public static class FilterSphere {
		public Vector3d center;
		public float radius;
		public float feather;

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
					0,
					0,
					0
			});

			buf.put(RenderUtil.writeMatrix(filter));
		}
	}
}
