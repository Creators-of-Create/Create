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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockStateExtensions {

	default boolean create$addRunningEffects(Level world, BlockPos pos, Entity entity) {
		return ((BlockExtensions) ((BlockState) this).getBlock()).create$addRunningEffects((BlockState) this, world, pos, entity);
	}

	default boolean create$addLandingEffects(ServerLevel worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return ((BlockExtensions) ((BlockState) this).getBlock()).create$addLandingEffects((BlockState) this, worldserver, pos, state2, entity, numberOfParticles);
	}

	@Environment(EnvType.CLIENT)
	default boolean create$addDestroyEffects(Level world, BlockPos pos, ParticleEngine manager) {
		return ((BlockExtensions) ((BlockState) this).getBlock()).create$addDestroyEffects((BlockState) this, world, pos, manager);
	}

	default boolean create$isFlammable(BlockGetter world, BlockPos pos, Direction face) {
		return ((BlockExtensions) ((BlockState) this).getBlock()).create$isFlammable((BlockState) this, world, pos, face);
	}

	default int create$getFlammability(BlockGetter world, BlockPos pos, Direction face) {
		return ((BlockExtensions) ((BlockState) this).getBlock()).create$getFlammability((BlockState) this, world, pos, face);
	}

	default void create$onNeighborChange(LevelReader world, BlockPos pos, BlockPos neighbor) {
		((BlockExtensions) ((BlockState) this).getBlock()).create$onNeighborChange((BlockState) this, world, pos, neighbor);
	}

	default float create$getSlipperiness(LevelReader world, BlockPos pos, @Nullable Entity entity)
	{
		return ((BlockExtensions) ((BlockState) this).getBlock()).create$getSlipperiness((BlockState) this, world, pos, entity);
	}
}
