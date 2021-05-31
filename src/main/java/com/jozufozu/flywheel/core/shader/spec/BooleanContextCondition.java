package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;
import com.mojang.serialization.Codec;

import net.minecraft.util.ResourceLocation;

public class BooleanContextCondition implements IContextCondition {

	public static final Codec<BooleanContextCondition> BOOLEAN_SUGAR = IGameStateProvider.CODEC.xmap(gameContext -> {
		if (gameContext instanceof IBooleanStateProvider) {
			return new BooleanContextCondition(((IBooleanStateProvider) gameContext));
		}

		return null;
	}, IContextCondition::contextProvider);
	protected final IBooleanStateProvider context;

	public BooleanContextCondition(IBooleanStateProvider context) {
		this.context = context;
	}

	@Override
	public ResourceLocation getID() {
		return context.getID();
	}

	@Override
	public IGameStateProvider contextProvider() {
		return context;
	}

	@Override
	public boolean get() {
		return context.isTrue();
	}
}
