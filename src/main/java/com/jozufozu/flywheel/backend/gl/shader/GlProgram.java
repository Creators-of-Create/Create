package com.jozufozu.flywheel.backend.gl.shader;

import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.util.RenderUtil;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public abstract class GlProgram extends GlObject {

	public final ResourceLocation name;

	protected GlProgram(Program program) {
		setHandle(program.program);
		this.name = program.name;
	}

	public void bind() {
		glUseProgram(handle());
	}

	public void unbind() {
		glUseProgram(0);
	}

	/**
	 * Retrieves the index of the uniform with the given name.
	 *
	 * @param uniform The name of the uniform to find the index of
	 * @return The uniform's index
	 */
	public int getUniformLocation(String uniform) {
		int index = glGetUniformLocation(this.handle(), uniform);

		if (index < 0) {
			Backend.log.debug("No active uniform '{}' exists in program '{}'. Could be unused.", uniform, this.name);
		}

		return index;
	}

	/**
	 * Binds a sampler uniform to the given texture unit.
	 *
	 * @param name    The name of the sampler uniform.
	 * @param binding The index of the texture unit.
	 * @return The sampler uniform's index.
	 * @throws NullPointerException If no uniform exists with the given name.
	 */
	public int setSamplerBinding(String name, int binding) {
		int samplerUniform = getUniformLocation(name);

		if (samplerUniform >= 0) {
			glUniform1i(samplerUniform, binding);
		}

		return samplerUniform;
	}

	protected static void uploadMatrixUniform(int uniform, Matrix4f mat) {
		glUniformMatrix4fv(uniform, false, RenderUtil.writeMatrix(mat));
	}

	@Override
	protected void deleteInternal(int handle) {
		glDeleteProgram(handle);
	}

}
