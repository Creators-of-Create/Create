package com.simibubi.create.modules.logistics.block.transposer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.logistics.block.belts.BeltAttachableLogisticalBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class TransposerBlock extends BeltAttachableLogisticalBlock {

	public static BooleanProperty POWERED = BlockStateProperties.POWERED;

	public TransposerBlock() {
		setDefaultState(getDefaultState().with(POWERED, false));
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TransposerTileEntity();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(POWERED));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.TRANSPOSER.get(getBlockFacing(state));
	}

	@Override
	protected boolean isVertical() {
		return false;
	}

	@Override
	protected BlockState getVerticalDefaultState() {
		return AllBlocks.VERTICAL_TRANSPOSER.getDefault();
	}

	@Override
	protected BlockState getHorizontalDefaultState() {
		return AllBlocks.TRANSPOSER.getDefault();
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		if (world.isRemote)
			return ActionResultType.SUCCESS;
		Direction blockFacing = getBlockFacing(state);
		BlockState newState = state;
		if (blockFacing.getAxis().isHorizontal())
			newState = state.with(HORIZONTAL_FACING, blockFacing.getOpposite());
		else
			newState = state.cycle(UPWARD);
		BlockPos pos = context.getPos();
		world.setBlockState(pos, newState);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TransposerTileEntity) {
			TransposerTileEntity transposer = (TransposerTileEntity) te;
			CompoundNBT compound = new CompoundNBT();
			transposer.write(compound);
			world.removeTileEntity(pos);
			world.setTileEntity(pos, TileEntity.create(compound));
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState stateForPlacement = super.getStateForPlacement(context);
		return stateForPlacement.with(POWERED, Boolean.valueOf(context.getWorld().isBlockPowered(context.getPos())));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		Direction blockFacing = getBlockFacing(state);
		if (fromPos.equals(pos.offset(blockFacing)) || fromPos.equals(pos.offset(blockFacing.getOpposite()))) {
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}

		if (!reactsToRedstone())
			return;

		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos)) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
		}
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return true;
	}

	protected boolean reactsToRedstone() {
		return true;
	}

	public static class Vertical extends TransposerBlock {
		@Override
		protected boolean isVertical() {
			return true;
		}
	}

}
