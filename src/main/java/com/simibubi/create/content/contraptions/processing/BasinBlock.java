package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
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
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.BASIN.create();
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		return ActionResultType.FAIL;
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		ItemStack heldItem = player.getHeldItem(handIn);

		try {
			BasinTileEntity te = getTileEntity(worldIn, pos);
			if (!heldItem.isEmpty()) {
				if (tryEmptyItemIntoBasin(worldIn, player, handIn, heldItem, te))
					return ActionResultType.SUCCESS;
				if (tryFillItemFromBasin(worldIn, player, handIn, heldItem, te))
					return ActionResultType.SUCCESS;
				return ActionResultType.SUCCESS;
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

	protected boolean tryEmptyItemIntoBasin(World worldIn, PlayerEntity player, Hand handIn, ItemStack heldItem,
		BasinTileEntity te) {
		if (!EmptyingByBasin.canItemBeEmptied(worldIn, heldItem))
			return false;

		Pair<FluidStack, ItemStack> emptyingResult = EmptyingByBasin.emptyItem(worldIn, heldItem, true);
		LazyOptional<IFluidHandler> capability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		IFluidHandler tank = capability.orElse(null);
		FluidStack fluidStack = emptyingResult.getFirst();

		if (tank == null || fluidStack.getAmount() != tank.fill(fluidStack, FluidAction.SIMULATE))
			return false;
		if (worldIn.isRemote)
			return true;

		ItemStack copyOfHeld = heldItem.copy();
		emptyingResult = EmptyingByBasin.emptyItem(worldIn, copyOfHeld, false);
		tank.fill(fluidStack, FluidAction.EXECUTE);

		if (!player.isCreative()) {
			if (copyOfHeld.isEmpty())
				player.setHeldItem(handIn, emptyingResult.getSecond());
			else {
				player.setHeldItem(handIn, copyOfHeld);
				player.inventory.placeItemBackInInventory(worldIn, emptyingResult.getSecond());
			}
		}
		return true;
	}

	protected boolean tryFillItemFromBasin(World world, PlayerEntity player, Hand handIn, ItemStack heldItem,
		BasinTileEntity te) {
		if (!GenericItemFilling.canItemBeFilled(world, heldItem))
			return false;

		LazyOptional<IFluidHandler> capability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		IFluidHandler tank = capability.orElse(null);

		if (tank == null)
			return false;

		for (int i = 0; i < tank.getTanks(); i++) {
			FluidStack fluid = tank.getFluidInTank(i);
			if (fluid.isEmpty())
				continue;
			int requiredAmountForItem = GenericItemFilling.getRequiredAmountForItem(world, heldItem, fluid.copy());
			if (requiredAmountForItem == -1)
				continue;
			if (requiredAmountForItem > fluid.getAmount())
				continue;
			
			if (world.isRemote)
				return true;

			if (player.isCreative())
				heldItem = heldItem.copy();
			ItemStack out = GenericItemFilling.fillItem(world, requiredAmountForItem, heldItem, fluid.copy());
			
			FluidStack copy = fluid.copy();
			copy.setAmount(requiredAmountForItem);
			tank.drain(copy, FluidAction.EXECUTE);
			
			if (!player.isCreative())
				player.inventory.placeItemBackInInventory(world, out);
			te.notifyUpdate();
			return true;
		}

		return false;
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		if (!AllBlocks.BASIN.has(worldIn.getBlockState(entityIn.getPosition())))
			return;
		if (!(entityIn instanceof ItemEntity))
			return;
		if (!entityIn.isAlive())
			return;
		ItemEntity itemEntity = (ItemEntity) entityIn;
		withTileEntityDo(worldIn, entityIn.getPosition(), te -> {
			ItemStack insertItem = ItemHandlerHelper.insertItem(te.inputInventory, itemEntity.getItem()
				.copy(), false);
			if (insertItem.isEmpty()) {
				itemEntity.remove();
				if (!itemEntity.world.isRemote)
					AllTriggers.triggerForNearbyPlayers(AllTriggers.BASIN_THROW, itemEntity.world,
						itemEntity.getPosition(), 3);
				return;
			}

			itemEntity.setItem(insertItem);
		});
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.BASIN_BLOCK_SHAPE;
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		updateDiagonalNeighbours(state, world, pos);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
		if (ctx.getEntity() instanceof ItemEntity)
			return AllShapes.BASIN_COLLISION_SHAPE;
		return getShape(state, reader, pos, ctx);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		updateDiagonalNeighbours(state, worldIn, pos);
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

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		BlockState state = super.getStateForPlacement(ctx);
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		return updateDiagonalState(state, world, pos);
	}

	protected void updateDiagonalNeighbours(BlockState state, World world, BlockPos pos) {
		for (Direction direction : Iterate.horizontalDirections) {
			BlockPos toUpdate = pos.up()
				.offset(direction);
			BlockState stateToUpdate = world.getBlockState(toUpdate);
			BlockState updated = updateDiagonalState(stateToUpdate, world, toUpdate);
			if (stateToUpdate != updated && !world.isRemote)
				world.setBlockState(toUpdate, updated);
		}
	}

	public static BlockState updateDiagonalState(BlockState state, IBlockReader world, BlockPos pos) {
		if (!(state.getBlock() instanceof BasinBlock))
			return state;
		for (Direction direction : Iterate.horizontalDirections) {
			BlockState diagonaloutputBasin = world.getBlockState(pos.down()
				.offset(direction));
			if (diagonaloutputBasin.getBlock() instanceof BasinBlock)
				return state.with(FACING, direction);
		}
		return state.with(FACING, Direction.DOWN);
	}

}
