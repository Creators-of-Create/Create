package com.simibubi.create.foundation.render.effects;

import java.util.ArrayList;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;

import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.loading.Program;

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

	public SphereFilterProgram(Program program) {
		super(program);

		effectsUBO = new GlBuffer(GlBufferType.UNIFORM_BUFFER);

		uniformBlock = GL31.glGetUniformBlockIndex(program.program, "Filters");

		GL31.glUniformBlockBinding(program.program, uniformBlock, UBO_BINDING);

		effectsUBO.bind();
		effectsUBO.alloc(BUFFER_SIZE);
		GL31.glBindBufferBase(effectsUBO.getBufferTarget().glEnum, UBO_BINDING, effectsUBO.handle());
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
		effectsUBO.bind(GlBufferType.ARRAY_BUFFER);
		MappedBuffer buffer = effectsUBO.getBuffer(0, BUFFER_SIZE)
				.putInt(filters.size())
				.position(16);

		filters.forEach(it -> it.write(buffer));

		buffer.flush();

		effectsUBO.unbind(GlBufferType.ARRAY_BUFFER);
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
