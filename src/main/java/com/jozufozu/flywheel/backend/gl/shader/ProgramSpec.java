package com.jozufozu.flywheel.backend.gl.shader;

import java.util.ArrayList;
import java.util.Arrays;

import com.jozufozu.flywheel.backend.gl.attrib.IVertexAttrib;
import com.simibubi.create.Create;

import net.minecraft.util.ResourceLocation;

public class ProgramSpec<P extends GlProgram> {

	public final ResourceLocation name;
	public final ResourceLocation vert;
	public final ResourceLocation frag;

	public final ShaderConstants defines;

	public final ArrayList<IVertexAttrib> attributes;

	public final ShaderSpecLoader<P> finalizer;

	public static <P extends GlProgram> Builder<P> builder(String name, ShaderSpecLoader<P> factory) {
		return builder(new ResourceLocation(Create.ID, name), factory);
	}

	public static <P extends GlProgram> Builder<P> builder(ResourceLocation name, ShaderSpecLoader<P> factory) {
		return new Builder<>(name, factory);
	}

	public ProgramSpec(ResourceLocation name, ResourceLocation vert, ResourceLocation frag, ShaderConstants defines, ArrayList<IVertexAttrib> attributes, ShaderSpecLoader<P> finalizer) {
		this.name = name;
		this.vert = vert;
		this.frag = frag;
		this.defines = defines;

		this.attributes = attributes;
		this.finalizer = finalizer;
	}

	public static class Builder<P extends GlProgram> {
		private ResourceLocation vert;
		private ResourceLocation frag;
		private ShaderConstants defines = ShaderConstants.EMPTY;
		private final ShaderSpecLoader<P> loader;

		private final ResourceLocation name;
		private final ArrayList<IVertexAttrib> attributes;

		public Builder(ResourceLocation name, ShaderSpecLoader<P> factory) {
			this.name = name;
			this.loader = factory;
			attributes = new ArrayList<>();
		}

		public Builder<P> setVert(ResourceLocation vert) {
			this.vert = vert;
			return this;
		}

		public Builder<P> setFrag(ResourceLocation frag) {
			this.frag = frag;
			return this;
		}

		public Builder<P> setDefines(ShaderConstants defines) {
			this.defines = defines;
			return this;
		}

		public <A extends Enum<A> & IVertexAttrib> Builder<P> addAttributes(Class<A> attributeEnum) {
			attributes.addAll(Arrays.asList(attributeEnum.getEnumConstants()));
			return this;
		}

		public ProgramSpec<P> createProgramSpec() {
			return new ProgramSpec<>(name, vert, frag, defines, attributes, loader);
		}
	}

}
