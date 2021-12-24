package com.simibubi.create.lib.extensions;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface BlockExtensions {
	default boolean create$addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity) {
		return false;
	}

	default boolean create$addLandingEffects(BlockState state1, ServerLevel worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return false;
	}

	@Environment(EnvType.CLIENT)
	default boolean create$addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
		return false;
	}

	default boolean create$isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return ((BlockStateExtensions) state).create$getFlammability(world, pos, face) > 0;
	}

	default int create$getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return ((FireBlockExtensions) Blocks.FIRE).create$invokeGetBurnOdd(state);
	}

	default SoundType create$getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
		return ((Block) this).getSoundType(state);
	}

	default int create$getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
		return state.getLightEmission();
	}

	default boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
		return state.getBlock() instanceof HalfTransparentBlock || state.getBlock() instanceof LeavesBlock;
	}

	default void create$onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {}

	default float create$getSlipperiness(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
		return ((Block) this).getFriction();
	}
}
