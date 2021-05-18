package com.jozufozu.flywheel.backend.loading;

import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_TRUE;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glLinkProgram;

import java.util.EnumMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

import net.minecraft.util.ResourceLocation;

public class Program {
	public final ResourceLocation name;
	public final int program;

	private int attributeIndex;

	public final Map<ShaderType, Shader> attached;

	public Program(ResourceLocation name) {
		this.name = name;
		this.program = glCreateProgram();
		attached = new EnumMap<>(ShaderType.class);
	}

	public Program attachShader(Shader shader, GlShader glShader) {
		glAttachShader(this.program, glShader.handle());

		attached.put(shader.type, shader);

		return this;
	}

	public Program addAttribute(String name, int attributeCount) {
		glBindAttribLocation(this.program, attributeIndex, name);
		attributeIndex += attributeCount;
		return this;
	}

	/**
	 * Links the attached shaders to this program.
	 */
	public Program link() {
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
