package com.jozufozu.flywheel.core.shader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.core.shader.spec.IContextCondition;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.core.shader.spec.ProgramState;
import com.jozufozu.flywheel.util.Pair;

public class StateSensitiveMultiProgram<P extends GlProgram> implements IMultiProgram<P> {

	List<Pair<IContextCondition, P>> variants;
	P fallback;

	public StateSensitiveMultiProgram(ShaderSources loader, ExtensibleGlProgram.Factory<P> factory, ShaderContext<P> context, ProgramSpec p) {
		variants = new ArrayList<>(p.states.size());

		for (ProgramState state : p.states) {

			Program variant = context.loadProgram(loader, p, state.getDefines());

			Pair<IContextCondition, P> pair = Pair.of(state.getContext(), factory.create(variant, state.getExtensions()));

			variants.add(pair);
		}

		fallback = factory.create(context.loadProgram(loader, p, Collections.emptyList()));
	}

	@Override
	public P get() {
		for (Pair<IContextCondition, P> variant : variants) {
			if (variant.getFirst().get())
				return variant.getSecond();
		}

		return fallback;
	}

	@Override
	public void delete() {
		for (Pair<IContextCondition, P> variant : variants) {
			variant.getSecond().delete();
		}

		fallback.delete();
	}
}
