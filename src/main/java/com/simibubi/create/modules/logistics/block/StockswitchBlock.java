package com.simibubi.create.modules.logistics.block;

import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.gui.ScreenOpener;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.CapabilityItemHandler;

public class StockswitchBlock extends HorizontalBlock implements ITE<StockswitchTileEntity> {

	public static final IntegerProperty INDICATOR = IntegerProperty.create("indicator", 0, 6);

	public StockswitchBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateObservedInventory(state, worldIn, pos);
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
		if (world.isRemote())
			return;
		if (!isObserving(state, pos, neighbor))
			return;
		updateObservedInventory(state, world, pos);
	}

	private void updateObservedInventory(BlockState state, IWorldReader world, BlockPos pos) {
		withTileEntityDo(world, pos, StockswitchTileEntity::updateCurrentLevel);
	}

	private boolean isObserving(BlockState state, BlockPos pos, BlockPos observing) {
		return observing.equals(pos.offset(state.get(HORIZONTAL_FACING)));
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return side != null && side.getOpposite() != state.get(HORIZONTAL_FACING);
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		try {
			return getTileEntity(blockAccess, pos).powered ? 15 : 0;
		} catch (TileEntityException e) {}
		return 0;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, INDICATOR);
		super.fillStateContainer(builder);
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (player instanceof ClientPlayerEntity)
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> withTileEntityDo(worldIn, pos, this::displayScreen));
		return ActionResultType.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(StockswitchTileEntity te) {
		ScreenOpener.open(new StockswitchScreen(te));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();

		Direction preferredFacing = null;
		for (Direction face : Direction.values()) {
			if (face.getAxis().isVertical())
				continue;

			TileEntity te = context.getWorld().getTileEntity(context.getPos().offset(face));
			if (te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent())
				if (preferredFacing == null)
					preferredFacing = face;
				else {
					preferredFacing = null;
					break;
				}
		}

		if (preferredFacing != null) {
			state = state.with(HORIZONTAL_FACING, preferredFacing);
		} else if (context.getFace().getAxis().isHorizontal()) {
			state = state.with(HORIZONTAL_FACING, context.getFace());
		} else {
			state = state.with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
		}

		return state;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new StockswitchTileEntity();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public Class<StockswitchTileEntity> getTileEntityClass() {
		return StockswitchTileEntity.class;
	}

}
