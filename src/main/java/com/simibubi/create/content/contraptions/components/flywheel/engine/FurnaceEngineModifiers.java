package com.simibubi.create.content.contraptions.components.flywheel.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.IRegistryDelegate;

public class FurnaceEngineModifiers {

	private final static FurnaceEngineModifiers INSTANCE = new FurnaceEngineModifiers();
	
	protected FurnaceEngineModifiers() {
		blockModifiers = new HashMap<>();
		blockActivators = new HashMap<>();
	}

	private final Map<IRegistryDelegate<Block>, Float> blockModifiers;
	private final Map<IRegistryDelegate<Block>, Function<BlockState, EngineState>> blockActivators;

	public void register(IRegistryDelegate<Block> block, float modifier) {
		this.blockModifiers.put(block, modifier);
	}
	
	public void register(IRegistryDelegate<Block> block, float modifier, Function<BlockState, EngineState> engineState) {
		this.blockModifiers.put(block, modifier);
		this.blockActivators.put(block, engineState);
	}

	private float getModifierOrDefault(BlockState state, float defaultValue) {
		return blockModifiers.getOrDefault(state.getBlock().delegate, defaultValue);
	}
	
	private Function<BlockState, EngineState> getEngineStateOrDefault(BlockState state, Function<BlockState, EngineState> engineState) {
		return blockActivators.getOrDefault(state.getBlock().delegate, engineState);
	}

	public float getModifier(BlockState state) {
		return getModifierOrDefault(state, 1f);
	}
	
	public EngineState getEngineState(BlockState state) {
		return getEngineStateOrDefault(state, 
				s -> s.getBlock() instanceof AbstractFurnaceBlock && s.hasProperty(AbstractFurnaceBlock.LIT) ? 
						(s.getValue(AbstractFurnaceBlock.LIT) ? EngineState.ACTIVE : EngineState.VALID) : EngineState.EMPTY).apply(state);
	}

	public static void register() {
		get().register(Blocks.BLAST_FURNACE.delegate, 2f);
		
		/*
		Example:
		get().register(Blocks.REDSTONE_LAMP.delegate, 1f, 
			s -> s.getBlock() instanceof RedstoneLampBlock && s.hasProperty(RedstoneLampBlock.LIT) ? 
				(s.getValue(RedstoneLampBlock.LIT) ? EngineState.ACTIVE : EngineState.VALID) : EngineState.EMPTY);
		*/
	}
	
	public static FurnaceEngineModifiers get() {
		return INSTANCE;
	}
	
	public enum EngineState {
		EMPTY,
		VALID,
		ACTIVE;
		
		public boolean isEngine() {
			return this != EMPTY;
		}
		
		public boolean isActive() {
			return this == ACTIVE;
		}
		
		public boolean isEmpty() {
			return this == EMPTY;
		}
	}
}
