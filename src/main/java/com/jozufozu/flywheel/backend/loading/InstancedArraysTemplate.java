package com.jozufozu.flywheel.backend.loading;

import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

import net.minecraft.util.ResourceLocation;

public class InstancedArraysTemplate extends ProgramTemplate {
	public static final String[] requiredVert = {"FLWInstanceData", "FLWVertexData", "FLWFragment"};
	public static final String[] requiredFrag = {"FLWFragment"};

	public static final ResourceLocation vert = new ResourceLocation("create", "template/instanced/instanced.vert");
	public static final ResourceLocation frag = new ResourceLocation("create", "template/instanced/instanced.frag");

	public InstancedArraysTemplate(ShaderLoader loader) {
		super(loader);

		templates.put(ShaderType.VERTEX, new ShaderTemplate(requiredVert, loader.getShaderSource(vert)));
		templates.put(ShaderType.FRAGMENT, new ShaderTemplate(requiredFrag, loader.getShaderSource(frag)));
	}
}
