package com.simibubi.create.content.logistics.block.redstone;

import java.util.Random;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
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
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.CapabilityItemHandler;

public class StockpileSwitchBlock extends HorizontalBlock implements ITE<StockpileSwitchTileEntity>, IWrenchable {

	public static final IntegerProperty INDICATOR = IntegerProperty.create("indicator", 0, 6);

	public StockpileSwitchBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
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

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.STOCKPILE_SWITCH.get(state.get(HORIZONTAL_FACING));
	}

	private void updateObservedInventory(BlockState state, IWorldReader world, BlockPos pos) {
		withTileEntityDo(world, pos, StockpileSwitchTileEntity::updateCurrentLevel);
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
		if (side == blockState.get(HORIZONTAL_FACING).getOpposite())
			return 0;
		return getTileEntityOptional(blockAccess, pos).filter(StockpileSwitchTileEntity::isPowered)
				.map($ -> 15)
				.orElse(0);
	}

	@Override
	public void scheduledTick(BlockState blockState, ServerWorld world, BlockPos pos, Random random) {
		getTileEntityOptional(world, pos).ifPresent(StockpileSwitchTileEntity::updatePowerAfterDelay);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, INDICATOR);
		super.fillStateContainer(builder);
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		if (player != null && AllItems.WRENCH.isIn(player.getHeldItem(handIn)))
			return ActionResultType.PASS;
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
			() -> () -> withTileEntityDo(worldIn, pos, te -> this.displayScreen(te, player)));
		return ActionResultType.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(StockpileSwitchTileEntity te, PlayerEntity player) {
		if (player instanceof ClientPlayerEntity)
			ScreenOpener.open(new StockpileSwitchScreen(te));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();

		Direction preferredFacing = null;
		for (Direction face : Iterate.horizontalDirections) {
			TileEntity te = context.getWorld()
				.getTileEntity(context.getPos()
					.offset(face));
			if (te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.isPresent())
				if (preferredFacing == null)
					preferredFacing = face;
				else {
					preferredFacing = null;
					break;
				}
		}

		if (preferredFacing != null) {
			state = state.with(HORIZONTAL_FACING, preferredFacing);
		} else if (context.getFace()
			.getAxis()
			.isHorizontal()) {
			state = state.with(HORIZONTAL_FACING, context.getFace());
		} else {
			state = state.with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing()
				.getOpposite());
		}

		return state;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.STOCKPILE_SWITCH.create();
	}

	@Override
	public Class<StockpileSwitchTileEntity> getTileEntityClass() {
		return StockpileSwitchTileEntity.class;
	}

}
