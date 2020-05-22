package com.simibubi.create.modules.logistics.block;

import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.modules.contraptions.IWrenchable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class RedstoneLinkBlock extends ProperDirectionalBlock implements ITE<RedstoneLinkTileEntity>, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty RECEIVER = BooleanProperty.create("receiver");

	public RedstoneLinkBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(POWERED, false).with(RECEIVER, false));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		Direction blockFacing = state.get(FACING);

		if (fromPos.equals(pos.offset(blockFacing.getOpposite()))) {
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}

		updateTransmittedSignal(state, worldIn, pos, blockFacing);
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateTransmittedSignal(state, worldIn, pos, state.get(FACING));
	}

	private void updateTransmittedSignal(BlockState state, World worldIn, BlockPos pos, Direction blockFacing) {
		if (worldIn.isRemote)
			return;
		if (state.get(RECEIVER))
			return;

		boolean shouldPower = worldIn.getWorld().isBlockPowered(pos);

		for (Direction direction : Iterate.directions) {
			BlockPos blockpos = pos.offset(direction);
			shouldPower |= worldIn.getRedstonePower(blockpos, Direction.UP) > 0;
		}

		boolean previouslyPowered = state.get(POWERED);

		if (previouslyPowered != shouldPower) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
			withTileEntityDo(worldIn, pos, te -> te.transmit(!previouslyPowered));
		}
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return state.get(POWERED) && state.get(RECEIVER);
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (side != blockState.get(FACING))
			return 0;
		return getWeakPower(blockState, blockAccess, pos, side);
	}

	@Override
	public int getWeakPower(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (!state.get(RECEIVER))
			return 0;
		return state.get(POWERED) ? 15 : 0;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWERED, RECEIVER);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new RedstoneLinkTileEntity();
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (player.isSneaking())
			return toggleMode(state, worldIn, pos);
		return ActionResultType.PASS;
	}

	public ActionResultType toggleMode(BlockState state, World worldIn, BlockPos pos) {
		if (worldIn.isRemote)
			return ActionResultType.SUCCESS;
		try {
			RedstoneLinkTileEntity te = getTileEntity(worldIn, pos);
			Boolean wasReceiver = state.get(RECEIVER);
			boolean blockPowered = worldIn.isBlockPowered(pos);
			worldIn.setBlockState(pos, state.cycle(RECEIVER).with(POWERED, blockPowered), 3);
			if (wasReceiver) {
				te.transmit(worldIn.isBlockPowered(pos));
			} else
				te.transmit(false);
			return ActionResultType.SUCCESS;
		} catch (TileEntityException e) {}
		return ActionResultType.PASS;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (toggleMode(state, context.getWorld(), context.getPos()) == ActionResultType.SUCCESS)
			return ActionResultType.SUCCESS;
		return IWrenchable.super.onWrenched(state, context);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return side != null;
	}

//	@Override // TODO 1.15 register layer
//	public BlockRenderLayer getRenderLayer() {
//		return BlockRenderLayer.CUTOUT;
//	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockPos neighbourPos = pos.offset(state.get(FACING).getOpposite());
		BlockState neighbour = worldIn.getBlockState(neighbourPos);
		return Block.hasSolidSide(neighbour, worldIn, neighbourPos, state.get(FACING));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();
		state = state.with(FACING, context.getFace());
		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.REDSTONE_BRIDGE.get(state.get(FACING));
	}

	@Override
	public Class<RedstoneLinkTileEntity> getTileEntityClass() {
		return RedstoneLinkTileEntity.class;
	}

}
