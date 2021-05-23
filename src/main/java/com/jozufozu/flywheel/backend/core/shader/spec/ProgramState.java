package com.jozufozu.flywheel.backend.core.shader.spec;

import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.backend.core.shader.extension.IProgramExtension;
import com.jozufozu.flywheel.util.CodecUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ProgramState {

	// TODO: Use Codec.dispatch
	private static final Codec<IContextCondition> WHEN = Codec.either(
			BooleanContextCondition.BOOLEAN_SUGAR,
			SpecificValueCondition.CODEC
			).flatXmap(
					either -> either.map(DataResult::success, DataResult::success),
					any -> {
						if (any instanceof BooleanContextCondition) {
							return DataResult.success(Either.left((BooleanContextCondition) any));
						}

						if (any instanceof SpecificValueCondition) {
							return DataResult.success(Either.right((SpecificValueCondition) any));
						}

						return DataResult.error("unknown context condition");
					}
			);

	public static final Codec<ProgramState> CODEC = RecordCodecBuilder.create(state ->
			state.group(
					WHEN.fieldOf("when")
							.forGetter(ProgramState::getContext),
					CodecUtil.oneOrMore(Codec.STRING)
							.optionalFieldOf("define", Collections.emptyList())
							.forGetter(ProgramState::getDefines),
					CodecUtil.oneOrMore(IProgramExtension.CODEC)
							.optionalFieldOf("extend", Collections.emptyList())
							.forGetter(ProgramState::getExtensions)
			).apply(state, ProgramState::new));

	private final IContextCondition context;
	private final List<String> defines;
	private final List<IProgramExtension> extensions;

	public ProgramState(IContextCondition context, List<String> defines, List<IProgramExtension> extensions) {
		this.context = context;
		this.defines = defines;
		this.extensions = extensions;
	}

	public IContextCondition getContext() {
		return context;
	}

	public List<String> getDefines() {
		return defines;
	}

	public List<IProgramExtension> getExtensions() {
		return extensions;
	}
}
