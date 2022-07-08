package com.simibubi.create.content.contraptions.components.fan;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.logistics.block.chute.AbstractChuteBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EncasedFanBlock extends DirectionalKineticBlock implements ITE<EncasedFanTileEntity> {

	public EncasedFanBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, worldIn, pos, oldState, isMoving);
		blockUpdate(state, worldIn, pos);
	}

	@Override
	public void updateIndirectNeighbourShapes(BlockState stateIn, LevelAccessor worldIn, BlockPos pos, int flags, int count) {
		super.updateIndirectNeighbourShapes(stateIn, worldIn, pos, flags, count);
		blockUpdate(stateIn, worldIn, pos);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState p_196243_4_, boolean p_196243_5_) {
		if (state.hasBlockEntity() && (state.getBlock() != p_196243_4_.getBlock() || !p_196243_4_.hasBlockEntity())) {
			withTileEntityDo(world, pos, EncasedFanTileEntity::updateChute);
			world.removeBlockEntity(pos);
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		blockUpdate(state, worldIn, pos);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction face = context.getClickedFace();

		BlockState placedOn = world.getBlockState(pos.relative(face.getOpposite()));
		BlockState placedOnOpposite = world.getBlockState(pos.relative(face));
		if (AbstractChuteBlock.isChute(placedOn))
			return defaultBlockState().setValue(FACING, face.getOpposite());
		if (AbstractChuteBlock.isChute(placedOnOpposite))
			return defaultBlockState().setValue(FACING, face);

		Direction preferredFacing = getPreferredFacing(context);
		if (preferredFacing == null)
			preferredFacing = context.getNearestLookingDirection();
		return defaultBlockState().setValue(FACING, context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown() ? preferredFacing : preferredFacing.getOpposite());
	}

	protected void blockUpdate(BlockState state, LevelAccessor worldIn, BlockPos pos) {
		if (worldIn instanceof WrappedWorld)
			return;
		notifyFanTile(worldIn, pos);
	}

	protected void notifyFanTile(LevelAccessor world, BlockPos pos) {
		withTileEntityDo(world, pos, EncasedFanTileEntity::blockInFrontChanged);
	}

	@Override
	public BlockState updateAfterWrenched(BlockState newState, UseOnContext context) {
		blockUpdate(newState, context.getLevel(), context.getClickedPos());
		return newState;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING)
			.getAxis();
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(FACING)
			.getOpposite();
	}

	@Override
	public Class<EncasedFanTileEntity> getTileEntityClass() {
		return EncasedFanTileEntity.class;
	}
	
	@Override
	public BlockEntityType<? extends EncasedFanTileEntity> getTileEntityType() {
		return AllTileEntities.ENCASED_FAN.get();
	}

}
