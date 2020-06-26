package com.simibubi.create.content.logistics.block.funnel;

import java.util.Collections;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.components.structureMovement.IPortableBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.logistics.block.AttachedLogisticalBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
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
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class FunnelBlock extends AttachedLogisticalBlock
		implements ITE<FunnelTileEntity>, IPortableBlock {

	public static final BooleanProperty BELT = BooleanProperty.create("belt");
	public static final MovementBehaviour MOVEMENT = new FunnelMovementBehaviour();

	public FunnelBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		if (!isVertical())
			builder.add(BELT);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.FUNNEL.create();
	}

	@Override
	protected boolean isVertical() {
		return false;
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (worldIn.isRemote)
			return;
		if (!(entityIn instanceof ItemEntity))
			return;
		ItemEntity itemEntity = (ItemEntity) entityIn;
		withTileEntityDo(worldIn, pos, te -> {
			ItemStack remainder = te.tryToInsert(itemEntity.getItem());
			if (remainder.isEmpty())
				itemEntity.remove();
			if (remainder.getCount() < itemEntity.getItem().getCount())
				itemEntity.setItem(remainder);
		});
	}

	@Override
	protected BlockState getVerticalDefaultState() {
		return AllBlocks.VERTICAL_FUNNEL.getDefaultState();
	}

	@Override
	protected BlockState getHorizontalDefaultState() {
		return AllBlocks.FUNNEL.getDefaultState();
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (facing == Direction.DOWN && !isVertical(stateIn))
			return stateIn.with(BELT, isOnBelt(worldIn, currentPos));
		return stateIn;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = super.getStateForPlacement(context);
		if (!isVertical(state)) {
			World world = context.getWorld();
			BlockPos pos = context.getPos();
			state = state.with(BELT, isOnBelt(world, pos));
		}
		return state;
	}

	protected boolean isOnBelt(IWorld world, BlockPos pos) {
		return AllBlocks.BELT.has(world.getBlockState(pos.down()));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction direction = getBlockFacing(state);
		if (!isVertical(state) && state.get(BELT))
			return AllShapes.BELT_FUNNEL.get(direction);
		return AllShapes.FUNNEL.get(direction);
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (worldIn.isRemote)
			return;

		if (isOnBelt(worldIn, pos)) {
			BeltTileEntity belt = BeltHelper.getSegmentTE(worldIn, pos.down());
			if (belt == null)
				return;

			BeltTileEntity controllerBelt = belt.getControllerTE();
			if (controllerBelt == null)
				return;

			controllerBelt.getInventory().forEachWithin(belt.index + .5f, .55f, (transportedItemStack) -> {
				controllerBelt.getInventory().eject(transportedItemStack);
				return Collections.emptyList();
			});
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
			worldIn.removeTileEntity(pos);
		}
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {

		if (hit.getFace() == getBlockFacing(state).getOpposite()) {
			if (!worldIn.isRemote)
				withTileEntityDo(worldIn, pos, te -> {
					ItemStack heldItem = player.getHeldItem(handIn).copy();
					ItemStack remainder = te.tryToInsert(heldItem);
					if (!ItemStack.areItemStacksEqual(remainder, heldItem))
						player.setHeldItem(handIn, remainder);
				});
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	public static class Vertical extends FunnelBlock {
		public Vertical(Properties properties) {
			super(properties);
		}

		@Override
		protected boolean isVertical() {
			return true;
		}
	}

	@Override
	public MovementBehaviour getMovementBehaviour() {
		return MOVEMENT;
	}

	@Override
	public Class<FunnelTileEntity> getTileEntityClass() {
		return FunnelTileEntity.class;
	}

}
