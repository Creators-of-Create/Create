package com.simibubi.create.content.contraptions.components.flywheel.engine;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class FurnaceEngineModifiers {
	
	public final static FurnaceEngineModifiers INSTANCE = new FurnaceEngineModifiers();
	
	protected Map<Block, Float> blockModifiers;
	
	public FurnaceEngineModifiers(Map<Block, Float> blockModifiers) {
		this.blockModifiers = blockModifiers;
	}
	
	public FurnaceEngineModifiers() {
		this(new HashMap<>());
	}
	
	public void set(Block block, float modifier) {
		this.blockModifiers.put(block, modifier);
	}
	
	public float getModifier(BlockState state, float def) {
		return blockModifiers.getOrDefault(state.getBlock(), def);
	}
	
	public float getModifier(BlockState state) {
		return getModifier(state, 1f);
	}
	
	public static void register() {
		INSTANCE.set(Blocks.BLAST_FURNACE, 2f);
	}
}
