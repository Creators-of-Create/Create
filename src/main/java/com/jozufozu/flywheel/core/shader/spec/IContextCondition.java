package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;

import net.minecraft.util.ResourceLocation;

public interface IContextCondition {

	ResourceLocation getID();

	IGameStateProvider contextProvider();

	boolean get();
}
