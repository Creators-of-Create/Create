package com.simibubi.create.lib.condition;

import java.util.function.Predicate;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class ModLoadedCondition implements Condition {
	private static final ResourceLocation NAME = new ResourceLocation("create", "mod_loaded");
	private final String modid;

	public ModLoadedCondition(String modid) {
		this.modid = modid;
	}

	@Override
	public String toString() {
		return "mod_loaded(\"" + modid + "\")";
	}

	@Override
	public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> stateContainer) {
		return (blockState) -> FabricLoader.getInstance().isModLoaded(modid);
	}
}
