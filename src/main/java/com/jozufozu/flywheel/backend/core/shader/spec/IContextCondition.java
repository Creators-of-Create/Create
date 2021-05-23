package com.jozufozu.flywheel.backend.core.shader.spec;

import com.jozufozu.flywheel.backend.core.shader.gamestate.IGameStateProvider;

import net.minecraft.util.ResourceLocation;

public interface IContextCondition {

	ResourceLocation getID();

	IGameStateProvider contextProvider();

	boolean get();
}
