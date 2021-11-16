package com.simibubi.create.content.contraptions.components.flywheel.engine;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FurnaceEngineModifiers {

	public final static FurnaceEngineModifiers INSTANCE = new FurnaceEngineModifiers();

	protected Map<Block, Float> blockModifiers = new HashMap<>();

	public void register(Block block, float modifier) {
		this.blockModifiers.put(block, modifier);
	}

	public float getModifierOrDefault(BlockState state, float defaultValue) {
		return blockModifiers.getOrDefault(state.getBlock(), defaultValue);
	}

	public float getModifier(BlockState state) {
		return getModifierOrDefault(state, 1f);
	}

	public static void register() {
		INSTANCE.register(Blocks.BLAST_FURNACE, 2f);
	}
}
