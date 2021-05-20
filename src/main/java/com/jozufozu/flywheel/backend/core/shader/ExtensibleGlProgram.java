package com.jozufozu.flywheel.backend.core.shader;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.loading.Program;

/**
 * A shader program that be arbitrarily "extended". This class can take in any number of program extensions, and
 * will initialize them and then call their {@link IProgramExtension#bind() bind} function every subsequent time this
 * program is bound. An "extension" is something that interacts with the shader program in a way that is invisible to
 * the caller using the program. This is used by some programs to implement the different fog modes. Other uses might
 * include binding extra textures to allow for blocks to have normal maps, for example. As the extensions are
 * per-program, this also allows for same extra specialization within a
 * {@link com.jozufozu.flywheel.backend.ShaderContext ShaderContext}.
 */
public class ExtensibleGlProgram extends GlProgram {

	protected final List<IProgramExtension> extensions;

	public ExtensibleGlProgram(Program program, @Nullable List<ProgramExtender> extensions) {
		super(program);

		if (extensions != null) {
			this.extensions = extensions.stream()
					.map(e -> e.create(this))
					.collect(Collectors.toList());
		} else {
			this.extensions = Collections.emptyList();
		}
	}

	@Override
	public void bind() {
		super.bind();

		extensions.forEach(IProgramExtension::bind);
	}

	@Override
	public String toString() {
		return "ExtensibleGlProgram{" +
				"name=" + name +
				", extensions=" + extensions +
				'}';
	}

	/**
	 * A factory interface to create {@link GlProgram}s parameterized by a list of extensions. This doesn't necessarily
	 * have to return an {@link ExtensibleGlProgram} if implementors want more flexibility for whatever reason.
	 */
	public interface Factory<P extends GlProgram> {

		@Nonnull
		P create(Program program, @Nullable List<ProgramExtender> fogFactory);
	}
}
