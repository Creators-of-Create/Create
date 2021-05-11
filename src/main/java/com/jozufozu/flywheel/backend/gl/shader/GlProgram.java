package com.jozufozu.flywheel.backend.gl.shader;

import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_TRUE;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.util.Collection;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.attrib.IVertexAttrib;
import com.jozufozu.flywheel.util.RenderUtil;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public abstract class GlProgram extends GlObject {

	public final ResourceLocation name;

	protected GlProgram(ResourceLocation name, int handle) {
		setHandle(handle);
		this.name = name;
	}

	public static Builder builder(ResourceLocation name) {
		return new Builder(name);
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

	public static class Builder {
		public final ResourceLocation name;
		public final int program;

		private int attributeIndex;

		public Builder(ResourceLocation name) {
			this.name = name;
			this.program = glCreateProgram();
		}

		public Builder attachShader(GlShader shader) {
			glAttachShader(this.program, shader.handle());

			return this;
		}

		public <A extends IVertexAttrib> Builder addAttributes(Collection<A> attributes) {
			attributes.forEach(this::addAttribute);
			return this;
		}

		public <A extends IVertexAttrib> Builder addAttribute(A attrib) {
			glBindAttribLocation(this.program, attributeIndex, attrib.attribName());
			attributeIndex += attrib.attribSpec().getAttributeCount();
			return this;
		}

		/**
		 * Links the attached shaders to this program.
		 */
		public Builder link() {
			glLinkProgram(this.program);

			String log = glGetProgramInfoLog(this.program);

			if (!log.isEmpty()) {
				Backend.log.debug("Program link log for " + this.name + ": " + log);
			}

			int result = glGetProgrami(this.program, GL_LINK_STATUS);

			if (result != GL_TRUE) {
				throw new RuntimeException("Shader program linking failed, see log for details");
			}

			return this;
		}
	}
}
