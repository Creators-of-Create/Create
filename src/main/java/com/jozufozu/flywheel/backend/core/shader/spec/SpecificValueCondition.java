package com.jozufozu.flywheel.backend.core.shader.spec;

import com.jozufozu.flywheel.backend.core.shader.gamestate.IGameStateProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ResourceLocation;

public class SpecificValueCondition implements IContextCondition {

	public static final Codec<SpecificValueCondition> CODEC = RecordCodecBuilder.create(condition -> condition.group(
			IGameStateProvider.CODEC.fieldOf("provider").forGetter(SpecificValueCondition::contextProvider),
			Codec.STRING.fieldOf("value").forGetter(SpecificValueCondition::getValue)
	).apply(condition, SpecificValueCondition::new));

	private final String required;
	private final IGameStateProvider context;

	public SpecificValueCondition(IGameStateProvider context, String required) {
		this.required = required;
		this.context = context;
	}

	@Override
	public ResourceLocation getID() {
		return context.getID();
	}

	public String getValue() {
		return required;
	}

	@Override
	public IGameStateProvider contextProvider() {
		return context;
	}

	@Override
	public boolean get() {
		return required.equals(context.getValue().toString());
	}
}
