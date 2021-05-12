package com.jozufozu.flywheel.backend.gl.shader;

import java.util.ArrayList;
import java.util.Arrays;

import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.gl.attrib.IVertexAttrib;
import com.jozufozu.flywheel.backend.loading.InstancedArraysTemplate;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.backend.loading.ShaderTransformer;

import net.minecraft.util.ResourceLocation;

public class ProgramSpec {

	public final ResourceLocation name;
	public final ResourceLocation vert;
	public final ResourceLocation frag;

	public final ShaderConstants defines;

	public final ArrayList<IVertexAttrib> attributes;

	public static Builder builder(ResourceLocation name) {
		return new Builder(name);
	}

	public ProgramSpec(ResourceLocation name, ResourceLocation vert, ResourceLocation frag, ShaderConstants defines, ArrayList<IVertexAttrib> attributes) {
		this.name = name;
		this.vert = vert;
		this.frag = frag;
		this.defines = defines;

		this.attributes = attributes;
	}

	public GlProgram.Builder loadProgram(ShaderContext<?> ctx, ShaderConstants defines, ShaderLoader loader) {
		InstancedArraysTemplate template = new InstancedArraysTemplate(loader);

		ShaderTransformer transformer = new ShaderTransformer()
				.pushStage(ctx.loadingStage(loader))
//				.pushStage(loader::processIncludes)
//				.pushStage(template)
				.pushStage(loader::processIncludes);

		if (defines != null)
			transformer.pushStage(defines);

		Shader vertexFile = loader.source(vert, ShaderType.VERTEX);
		Shader fragmentFile = loader.source(frag, ShaderType.FRAGMENT);
		return loader.loadProgram(name, transformer, vertexFile, fragmentFile)
				.addAttributes(attributes);
	}

	public static class Builder {
		private ResourceLocation vert;
		private ResourceLocation frag;
		private ShaderConstants defines = ShaderConstants.EMPTY;

		private final ResourceLocation name;
		private final ArrayList<IVertexAttrib> attributes;

		public Builder(ResourceLocation name) {
			this.name = name;
			attributes = new ArrayList<>();
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

		public <A extends Enum<A> & IVertexAttrib> Builder addAttributes(Class<A> attributeEnum) {
			attributes.addAll(Arrays.asList(attributeEnum.getEnumConstants()));
			return this;
		}

		public ProgramSpec createProgramSpec() {
			return new ProgramSpec(name, vert, frag, defines, attributes);
		}
	}

}
