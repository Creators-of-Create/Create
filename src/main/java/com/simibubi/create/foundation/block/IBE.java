package com.simibubi.create.foundation.block;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntityTicker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface IBE<T extends BlockEntity> extends EntityBlock {

	Class<T> getBlockEntityClass();

	BlockEntityType<? extends T> getBlockEntityType();

	default void withBlockEntityDo(BlockGetter world, BlockPos pos, Consumer<T> action) {
		getBlockEntityOptional(world, pos).ifPresent(action);
	}

	default InteractionResult onBlockEntityUse(BlockGetter world, BlockPos pos, Function<T, InteractionResult> action) {
		return getBlockEntityOptional(world, pos).map(action)
			.orElse(InteractionResult.PASS);
	}

	/**
	 * if the IBE is bound to a SmartBlockEntity, which implements destroy(),<br>
	 * call this method in BlockBehaviour::onRemove (replace super call)
	 */
	public static void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState) {
		if (!blockState.hasBlockEntity())
			return;
		if (blockState.is(newBlockState.getBlock()) && newBlockState.hasBlockEntity())
			return;
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof SmartBlockEntity sbe)
			sbe.destroy();
		level.removeBlockEntity(pos);
	}

	default Optional<T> getBlockEntityOptional(BlockGetter world, BlockPos pos) {
		return Optional.ofNullable(getBlockEntity(world, pos));
	}

	@Override
	default BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return getBlockEntityType().create(p_153215_, p_153216_);
	}

	@Override
	default <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level p_153212_, BlockState p_153213_,
		BlockEntityType<S> p_153214_) {
		if (SmartBlockEntity.class.isAssignableFrom(getBlockEntityClass()))
			return new SmartBlockEntityTicker<>();
		return null;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	default T getBlockEntity(BlockGetter worldIn, BlockPos pos) {
		BlockEntity blockEntity = worldIn.getBlockEntity(pos);
		Class<T> expectedClass = getBlockEntityClass();

		if (blockEntity == null)
			return null;
		if (!expectedClass.isInstance(blockEntity))
			return null;

		return (T) blockEntity;
	}

}
