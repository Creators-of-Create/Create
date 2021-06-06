package com.jozufozu.flywheel.core.shader.gamestate;

import com.jozufozu.flywheel.backend.SpecMetaRegistry;
import com.mojang.serialization.Codec;

import net.minecraft.util.ResourceLocation;

public interface IGameStateProvider {

	Codec<IGameStateProvider> CODEC = ResourceLocation.CODEC.xmap(SpecMetaRegistry::getStateProvider, IGameStateProvider::getID);

	ResourceLocation getID();

	Object getValue();
}
