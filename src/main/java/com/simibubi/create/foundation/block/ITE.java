package com.simibubi.create.foundation.block;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public interface ITE<T extends BlockEntity> {

	Class<T> getTileEntityClass();

	default void withTileEntityDo(BlockGetter world, BlockPos pos, Consumer<T> action) {
		getTileEntityOptional(world, pos).ifPresent(action);
	}

	default InteractionResult onTileEntityUse(BlockGetter world, BlockPos pos, Function<T, InteractionResult> action) {
		return getTileEntityOptional(world, pos).map(action)
				.orElse(InteractionResult.PASS);
	}

	default Optional<T> getTileEntityOptional(BlockGetter world, BlockPos pos) {
		return Optional.ofNullable(getTileEntity(world, pos));
	}

	@Nullable
	@SuppressWarnings("unchecked")
	default T getTileEntity(BlockGetter worldIn, BlockPos pos) {
		BlockEntity tileEntity = worldIn.getBlockEntity(pos);
		Class<T> expectedClass = getTileEntityClass();

		if (tileEntity == null)
			return null;
		if (!expectedClass.isInstance(tileEntity))
			return null;

		return (T) tileEntity;
	}

}
