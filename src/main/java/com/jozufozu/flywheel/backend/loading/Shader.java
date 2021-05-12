package com.jozufozu.flywheel.backend.loading;

import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

import net.minecraft.util.ResourceLocation;

public class Shader {
	public ShaderType type;
	public ResourceLocation name;
	private String source;

	public Shader(ShaderType type, ResourceLocation name, String source) {
		this.type = type;
		this.name = name;
		this.setSource(source);
	}

	public static Shader vert(ResourceLocation fileLoc, String source) {
		return new Shader(ShaderType.VERTEX, fileLoc, source);
	}

	public static Shader frag(ResourceLocation fileLoc, String source) {
		return new Shader(ShaderType.FRAGMENT, fileLoc, source);
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
