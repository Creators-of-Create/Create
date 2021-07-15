package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
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
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import net.minecraft.block.AbstractBlock.Properties;

public class BasinBlock extends Block implements ITE<BasinTileEntity>, IWrenchable {

	public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;

	public BasinBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> p_206840_1_) {
		super.createBlockStateDefinition(p_206840_1_.add(FACING));
	}

	@Override
	public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
		TileEntity tileEntity = world.getBlockEntity(pos.above());
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
		if (!context.getLevel().isClientSide)
			withTileEntityDo(context.getLevel(), context.getClickedPos(), bte -> bte.onWrenched(context.getClickedFace()));
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		ItemStack heldItem = player.getItemInHand(handIn);

		return onTileEntityUse(worldIn, pos, te -> {
			if (!heldItem.isEmpty()) {
				if (FluidHelper.tryEmptyItemIntoTE(worldIn, player, handIn, heldItem, te))
					return ActionResultType.SUCCESS;
				if (FluidHelper.tryFillItemFromTE(worldIn, player, handIn, heldItem, te))
					return ActionResultType.SUCCESS;

				if (EmptyingByBasin.canItemBeEmptied(worldIn, heldItem)
						|| GenericItemFilling.canItemBeFilled(worldIn, heldItem))
					return ActionResultType.SUCCESS;
				if (heldItem.getItem()
					.equals(Items.SPONGE)
					&& !te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
						.map(iFluidHandler -> iFluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE))
						.orElse(FluidStack.EMPTY)
						.isEmpty()) {
					return ActionResultType.SUCCESS;
				}
				return ActionResultType.PASS;
			}

			IItemHandlerModifiable inv = te.itemCapability.orElse(new ItemStackHandler(1));
			boolean success = false;
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemStack stackInSlot = inv.getStackInSlot(slot);
				if (stackInSlot.isEmpty())
					continue;
				player.inventory.placeItemBackInInventory(worldIn, stackInSlot);
				inv.setStackInSlot(slot, ItemStack.EMPTY);
				success = true;
			}
			if (success)
				worldIn.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, .2f,
						1f + Create.RANDOM.nextFloat());
			te.onEmptied();
			return ActionResultType.SUCCESS;
		});
	}

	@Override
	public void updateEntityAfterFallOn(IBlockReader worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);
		if (!AllBlocks.BASIN.has(worldIn.getBlockState(entityIn.blockPosition())))
			return;
		if (!(entityIn instanceof ItemEntity))
			return;
		if (!entityIn.isAlive())
			return;
		ItemEntity itemEntity = (ItemEntity) entityIn;
		withTileEntityDo(worldIn, entityIn.blockPosition(), te -> {

			// Tossed items bypass the quarter-stack limit
			te.inputInventory.withMaxStackSize(64);
			ItemStack insertItem = ItemHandlerHelper.insertItem(te.inputInventory, itemEntity.getItem()
				.copy(), false);
			te.inputInventory.withMaxStackSize(16);

			if (insertItem.isEmpty()) {
				itemEntity.remove();
				if (!itemEntity.level.isClientSide)
					AllTriggers.triggerForNearbyPlayers(AllTriggers.BASIN_THROW, itemEntity.level,
						itemEntity.blockPosition(), 3);
				return;
			}

			itemEntity.setItem(insertItem);
		});
	}

	@Override
	public VoxelShape getInteractionShape(BlockState p_199600_1_, IBlockReader p_199600_2_, BlockPos p_199600_3_) {
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
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.getBlock() == newState.getBlock())
			return;
		TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
		withTileEntityDo(worldIn, pos, te -> {
			ItemHelper.dropContents(worldIn, pos, te.inputInventory);
			ItemHelper.dropContents(worldIn, pos, te.outputInventory);
			te.spoutputBuffer.forEach(is -> Block.popResource(worldIn, pos, is));
		});
		worldIn.removeBlockEntity(pos);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
		return getTileEntityOptional(worldIn, pos).map(BasinTileEntity::getInputInventory)
			.map(ItemHelper::calcRedstoneFromInventory)
			.orElse(0);
	}

	@Override
	public Class<BasinTileEntity> getTileEntityClass() {
		return BasinTileEntity.class;
	}

	public static boolean canOutputTo(IBlockReader world, BlockPos basinPos, Direction direction) {
		BlockPos neighbour = basinPos.relative(direction);
		BlockPos output = neighbour.below();
		BlockState blockState = world.getBlockState(neighbour);

		if (FunnelBlock.isFunnel(blockState)) {
			if (FunnelBlock.getFunnelFacing(blockState) == direction)
				return false;
		} else if (!blockState.getCollisionShape(world, neighbour)
			.isEmpty()) {
			return false;
		} else {
			TileEntity tileEntity = world.getBlockEntity(output);
			if (tileEntity instanceof BeltTileEntity) {
				BeltTileEntity belt = (BeltTileEntity) tileEntity;
				return belt.getSpeed() == 0 || belt.getMovementFacing() != direction.getOpposite();
			}
		}

		DirectBeltInputBehaviour directBeltInputBehaviour =
			TileEntityBehaviour.get(world, output, DirectBeltInputBehaviour.TYPE);
		if (directBeltInputBehaviour != null)
			return directBeltInputBehaviour.canInsertFromSide(direction);
		return false;
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
