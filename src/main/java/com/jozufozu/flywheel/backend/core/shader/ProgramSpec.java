package com.jozufozu.flywheel.backend.core.shader;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;

public class ProgramSpec {

	public final ResourceLocation name;
	public final ResourceLocation vert;
	public final ResourceLocation frag;

	public final List<ResourceLocation> debugModes = new ArrayList<>();

	public ProgramSpec(ResourceLocation name, ResourceLocation vert, ResourceLocation frag) {
		this.name = name;
		this.vert = vert;
		this.frag = frag;
	}

}
