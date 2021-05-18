package com.jozufozu.flywheel.backend.gl.shader;

import net.minecraft.util.ResourceLocation;

public class ProgramSpec {

	public final ResourceLocation name;
	public final ResourceLocation vert;
	public final ResourceLocation frag;

	public final ShaderConstants defines;

	public static Builder builder(ResourceLocation name) {
		return new Builder(name);
	}

	public ProgramSpec(ResourceLocation name, ResourceLocation vert, ResourceLocation frag, ShaderConstants defines) {
		this.name = name;
		this.vert = vert;
		this.frag = frag;
		this.defines = defines;
	}

	public static class Builder {
		private ResourceLocation vert;
		private ResourceLocation frag;
		private ShaderConstants defines = ShaderConstants.EMPTY;

		private final ResourceLocation name;

		public Builder(ResourceLocation name) {
			this.name = name;
		}

		public Builder setVert(ResourceLocation vert) {
			this.vert = vert;
			return this;
		}

		public Builder setFrag(ResourceLocation frag) {
			this.frag = frag;
			return this;
		}

		public Builder setDefines(ShaderConstants defines) {
			this.defines = defines;
			return this;
		}

		public ProgramSpec build() {
			return new ProgramSpec(name, vert, frag, defines);
		}
	}

}
