package com.simibubi.create.foundation.render.effects;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;

import com.jozufozu.flywheel.backend.gl.GlBuffer;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

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

	public void uploadFilters(ArrayList<FilterSphere> filters) {
		effectsUBO.bind(GL20.GL_ARRAY_BUFFER);
		effectsUBO.map(GL20.GL_ARRAY_BUFFER, 0, BUFFER_SIZE, buf -> {
			buf.putInt(filters.size());
			buf.position(16);
			FloatBuffer floatBuffer = buf.asFloatBuffer();

			filters.forEach(it -> it.write(floatBuffer));
		});
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

}
