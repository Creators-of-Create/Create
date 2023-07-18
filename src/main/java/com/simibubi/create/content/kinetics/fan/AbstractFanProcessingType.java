package com.simibubi.create.content.kinetics.fan;

import com.google.common.collect.Maps;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.LitBlazeBurnerBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

import static com.simibubi.create.content.processing.burner.BlazeBurnerBlock.getHeatLevelOf;

public abstract class AbstractFanProcessingType {

	public static final Map<String, AbstractFanProcessingType> REGISTRY = Maps.newConcurrentMap();
	public static final AbstractFanProcessingType NONE = new AbstractFanProcessingType("NONE") {
		@Override
		public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		}

		@Override
		public void affectEntity(Entity entity, Level level) {
		}

		@Override
		public void particleMorphType(AirFlowParticle particle) {

		}

		@Override
		public List<ItemStack> process(ItemStack stack, AbstractFanProcessingType type, Level world) {
			return null;
		}

		@Override
		public boolean canProcess(ItemStack stack, Level level) {
			return false;
		}
	};

	public final String name;

	public AbstractFanProcessingType(String name) {
		this.name = name;
		AbstractFanProcessingType old = REGISTRY.put(name, this);
		if (old != null) {
			throw new IllegalArgumentException("repeated processing type name: "
					+ name + " for class " + old.getClass().getCanonicalName()
					+ " and class " + this.getClass().getCanonicalName());
		}
	}

	public abstract boolean canProcess(ItemStack stack, Level level);

	public abstract void spawnParticlesForProcessing(Level level, Vec3 pos);

	public abstract void affectEntity(Entity entity, Level level);

	public abstract void particleMorphType(AirFlowParticle particle);

	public abstract List<ItemStack> process(ItemStack stack, AbstractFanProcessingType type, Level world);

	public static AbstractFanProcessingType byBlock(BlockGetter reader, BlockPos pos) {
		FluidState fluidState = reader.getFluidState(pos);
		if (fluidState.getType() == Fluids.WATER || fluidState.getType() == Fluids.FLOWING_WATER)
			return FanProcessing.SPLASHING;
		BlockState blockState = reader.getBlockState(pos);
		Block block = blockState.getBlock();
		if (block == Blocks.SOUL_FIRE
				|| block == Blocks.SOUL_CAMPFIRE && blockState.getOptionalValue(CampfireBlock.LIT)
				.orElse(false)
				|| AllBlocks.LIT_BLAZE_BURNER.has(blockState)
				&& blockState.getOptionalValue(LitBlazeBurnerBlock.FLAME_TYPE)
				.map(flame -> flame == LitBlazeBurnerBlock.FlameType.SOUL)
				.orElse(false))
			return FanProcessing.HAUNTING;
		if (block == Blocks.FIRE
				|| blockState.is(BlockTags.CAMPFIRES) && blockState.getOptionalValue(CampfireBlock.LIT)
				.orElse(false)
				|| AllBlocks.LIT_BLAZE_BURNER.has(blockState)
				&& blockState.getOptionalValue(LitBlazeBurnerBlock.FLAME_TYPE)
				.map(flame -> flame == LitBlazeBurnerBlock.FlameType.REGULAR)
				.orElse(false)
				|| getHeatLevelOf(blockState) == BlazeBurnerBlock.HeatLevel.SMOULDERING)
			return FanProcessing.SMOKING;
		if (block == Blocks.LAVA || getHeatLevelOf(blockState).isAtLeast(BlazeBurnerBlock.HeatLevel.FADING))
			return FanProcessing.BLASTING;
		return AbstractFanProcessingType.NONE;
	}

	public static AbstractFanProcessingType valueOf(String name) {
		return REGISTRY.getOrDefault(name, NONE);
	}

	public String name() {
		return this.name;
	}

}
