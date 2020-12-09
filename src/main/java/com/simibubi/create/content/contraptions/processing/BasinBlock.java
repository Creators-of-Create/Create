package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.DirectionProperty;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class BasinBlock extends Block implements ITE<BasinTileEntity>, IWrenchable {

	public static final DirectionProperty FACING = BlockStateProperties.FACING_EXCEPT_UP;

	public BasinBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		setDefaultState(getDefaultState().with(FACING, Direction.DOWN));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> p_206840_1_) {
		super.fillStateContainer(p_206840_1_.add(FACING));
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos.up());
		if (tileEntity instanceof BasinOperatingTileEntity)
			return false;
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.BASIN.create();
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (!context.getWorld().isRemote)
			withTileEntityDo(context.getWorld(), context.getPos(), bte -> bte.onWrenched(context.getFace()));
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		ItemStack heldItem = player.getHeldItem(handIn);

		try {
			BasinTileEntity te = getTileEntity(worldIn, pos);
			if (!heldItem.isEmpty()) {
				if (FluidHelper.tryEmptyItemIntoTE(worldIn, player, handIn, heldItem, te))
					return ActionResultType.SUCCESS;
				if (FluidHelper.tryFillItemFromTE(worldIn, player, handIn, heldItem, te))
					return ActionResultType.SUCCESS;

				if (EmptyingByBasin.canItemBeEmptied(worldIn, heldItem)
					|| GenericItemFilling.canItemBeFilled(worldIn, heldItem))
					return ActionResultType.SUCCESS;
				return ActionResultType.PASS;
			}

			IItemHandlerModifiable inv = te.itemCapability.orElse(new ItemStackHandler(1));
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				player.inventory.placeItemBackInInventory(worldIn, inv.getStackInSlot(slot));
				inv.setStackInSlot(slot, ItemStack.EMPTY);
			}
			te.onEmptied();
		} catch (TileEntityException e) {
		}

		return ActionResultType.SUCCESS;
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		if (!AllBlocks.BASIN.has(worldIn.getBlockState(entityIn.getBlockPos())))
			return;
		if (!(entityIn instanceof ItemEntity))
			return;
		if (!entityIn.isAlive())
			return;
		ItemEntity itemEntity = (ItemEntity) entityIn;
		withTileEntityDo(worldIn, entityIn.getBlockPos(), te -> {
			ItemStack insertItem = ItemHandlerHelper.insertItem(te.inputInventory, itemEntity.getItem()
				.copy(), false);
			if (insertItem.isEmpty()) {
				itemEntity.remove();
				if (!itemEntity.world.isRemote)
					AllTriggers.triggerForNearbyPlayers(AllTriggers.BASIN_THROW, itemEntity.world,
						itemEntity.getBlockPos(), 3);
				return;
			}

			itemEntity.setItem(insertItem);
		});
	}

	@Override
	public VoxelShape getRaytraceShape(BlockState p_199600_1_, IBlockReader p_199600_2_, BlockPos p_199600_3_) {
		return AllShapes.BASIN_RAYTRACE_SHAPE;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.BASIN_BLOCK_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
		if (ctx.getEntity() instanceof ItemEntity)
			return AllShapes.BASIN_COLLISION_SHAPE;
		return getShape(state, reader, pos, ctx);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.getBlock() == newState.getBlock())
			return;
		TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
		withTileEntityDo(worldIn, pos, te -> {
			ItemHelper.dropContents(worldIn, pos, te.inputInventory);
			ItemHelper.dropContents(worldIn, pos, te.outputInventory);
		});
		worldIn.removeTileEntity(pos);
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		try {
			return ItemHelper.calcRedstoneFromInventory(getTileEntity(worldIn, pos).inputInventory);
		} catch (TileEntityException e) {
		}
		return 0;
	}

	@Override
	public Class<BasinTileEntity> getTileEntityClass() {
		return BasinTileEntity.class;
	}

	public static boolean canOutputTo(IBlockReader world, BlockPos basinPos, Direction direction) {
		BlockPos neighbour = basinPos.offset(direction);
		if (!world.getBlockState(neighbour)
			.getCollisionShape(world, neighbour)
			.isEmpty())
			return false;

		BlockPos offset = basinPos.down()
			.offset(direction);
		DirectBeltInputBehaviour directBeltInputBehaviour =
			TileEntityBehaviour.get(world, offset, DirectBeltInputBehaviour.TYPE);
		if (directBeltInputBehaviour != null)
			return directBeltInputBehaviour.canInsertFromSide(direction);
		return false;
	}

}
