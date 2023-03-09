package com.simibubi.create.foundation.block;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.SmartTileEntityTicker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface ITE<T extends BlockEntity> extends EntityBlock {

	Class<T> getTileEntityClass();

	BlockEntityType<? extends T> getTileEntityType();

	default void withTileEntityDo(BlockGetter world, BlockPos pos, Consumer<T> action) {
		getTileEntityOptional(world, pos).ifPresent(action);
	}

	default InteractionResult onTileEntityUse(BlockGetter world, BlockPos pos, Function<T, InteractionResult> action) {
		return getTileEntityOptional(world, pos).map(action)
			.orElse(InteractionResult.PASS);
	}

	/**
	 * if the ITE is bound to a SmartTileEntity, which implements destroy(),<br>
	 * call this method in BlockBehaviour::onRemove (replace super call)
	 */
	public static void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState) {
		if (!blockState.hasBlockEntity())
			return;
		if (blockState.is(newBlockState.getBlock()) && newBlockState.hasBlockEntity())
			return;
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof SmartTileEntity ste)
			ste.destroy();
		level.removeBlockEntity(pos);
	}

	default Optional<T> getTileEntityOptional(BlockGetter world, BlockPos pos) {
		return Optional.ofNullable(getTileEntity(world, pos));
	}

	@Override
	default BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return getTileEntityType().create(p_153215_, p_153216_);
	}

	@Override
	default <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level p_153212_, BlockState p_153213_,
		BlockEntityType<S> p_153214_) {
		if (SmartTileEntity.class.isAssignableFrom(getTileEntityClass()))
			return new SmartTileEntityTicker<>();
		return null;
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
