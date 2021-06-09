package com.simibubi.create.foundation.block;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public interface ITE<T extends TileEntity> {

	Class<T> getTileEntityClass();

	default void withTileEntityDo(IBlockReader world, BlockPos pos, Consumer<T> action) {
		getTileEntityOptional(world, pos).ifPresent(action);
	}

	default ActionResultType onTileEntityUse(IBlockReader world, BlockPos pos, Function<T, ActionResultType> action) {
		return getTileEntityOptional(world, pos).map(action)
			.orElse(ActionResultType.PASS);
	}
	
	default Optional<T> getTileEntityOptional(IBlockReader world, BlockPos pos) {
		return Optional.ofNullable(getTileEntity(world, pos));
	}

	@Nullable
	@SuppressWarnings("unchecked")
	default T getTileEntity(IBlockReader worldIn, BlockPos pos) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		Class<T> expectedClass = getTileEntityClass();

		if (tileEntity == null)
			return null;
		if (!expectedClass.isInstance(tileEntity))
			return null;

		return (T) tileEntity;
	}

}
