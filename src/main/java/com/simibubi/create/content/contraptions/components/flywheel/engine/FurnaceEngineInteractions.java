package com.simibubi.create.content.contraptions.components.flywheel.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.IRegistryDelegate;

/**
 * Example:
 *
 * <pre>
 * {@code
 * FurnaceEngineInteractions.registerHandler(Blocks.REDSTONE_LAMP.delegate, FurnaceEngineInteractions.InteractionHandler.of(
 * 	s -> s.getBlock() instanceof RedstoneLampBlock && s.hasProperty(RedstoneLampBlock.LIT) ? 
 * 		(s.getValue(RedstoneLampBlock.LIT) ? HeatSource.ACTIVE : HeatSource.VALID) : HeatSource.EMPTY, s -> 1.5f));
 * }
 * </pre>
 */
public class FurnaceEngineInteractions {

	private static final Map<IRegistryDelegate<Block>, InteractionHandler> HANDLERS = new HashMap<>();
	private static final InteractionHandler DEFAULT_HANDLER = new InteractionHandler() {};

	public static void registerHandler(IRegistryDelegate<Block> block, InteractionHandler handler) {
		HANDLERS.put(block, handler);
	}

	public static InteractionHandler getHandler(IRegistryDelegate<Block> delegate) {
		return HANDLERS.getOrDefault(delegate, DEFAULT_HANDLER);
	}

	public static InteractionHandler getHandler(BlockState state) {
		return getHandler(state.getBlock().delegate);
	}

	public static void registerDefaults() {
		registerHandler(Blocks.BLAST_FURNACE.delegate, InteractionHandler.ofCustomSpeedModifier(state -> 2f));
	}

	public interface InteractionHandler {
		default HeatSource getHeatSource(BlockState state) {
			if (state.getBlock() instanceof AbstractFurnaceBlock && state.hasProperty(AbstractFurnaceBlock.LIT)) {
				if (state.getValue(AbstractFurnaceBlock.LIT)) {
					return HeatSource.ACTIVE;
				} else {
					return HeatSource.VALID;
				}
			}
			return HeatSource.EMPTY;
		}

		default float getSpeedModifier(BlockState state) {
			return 1f;
		};

		static InteractionHandler of(Function<BlockState, HeatSource> heatSourceFunc, Function<BlockState, Float> speedModifierFunc) {
			return new InteractionHandler() {
				@Override
				public HeatSource getHeatSource(BlockState state) {
					return heatSourceFunc.apply(state);
				}

				@Override
				public float getSpeedModifier(BlockState state) {
					return speedModifierFunc.apply(state);
				}
			};
		}

		static InteractionHandler ofCustomHeatSource(Function<BlockState, HeatSource> heatSourceFunc) {
			return new InteractionHandler() {
				@Override
				public HeatSource getHeatSource(BlockState state) {
					return heatSourceFunc.apply(state);
				}
			};
		}

		static InteractionHandler ofCustomSpeedModifier(Function<BlockState, Float> speedModifierFunc) {
			return new InteractionHandler() {
				@Override
				public float getSpeedModifier(BlockState state) {
					return speedModifierFunc.apply(state);
				}
			};
		}
	}

	public enum HeatSource {
		EMPTY,
		VALID,
		ACTIVE;

		public boolean isEmpty() {
			return this == EMPTY;
		}

		public boolean isValid() {
			return this != EMPTY;
		}

		public boolean isActive() {
			return this == ACTIVE;
		}
	}
}
