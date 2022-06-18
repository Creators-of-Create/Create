package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class BasinBlock extends Block implements ITE<BasinTileEntity>, IWrenchable {

	public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;

	public BasinBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> p_206840_1_) {
		super.createBlockStateDefinition(p_206840_1_.add(FACING));
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockEntity tileEntity = world.getBlockEntity(pos.above());
		if (tileEntity instanceof BasinOperatingTileEntity)
			return false;
		return true;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (!context.getLevel().isClientSide)
			withTileEntityDo(context.getLevel(), context.getClickedPos(),
				bte -> bte.onWrenched(context.getClickedFace()));
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		ItemStack heldItem = player.getItemInHand(handIn);

		return onTileEntityUse(worldIn, pos, te -> {
			if (!heldItem.isEmpty()) {
				if (FluidHelper.tryEmptyItemIntoTE(worldIn, player, handIn, heldItem, te))
					return InteractionResult.SUCCESS;
				if (FluidHelper.tryFillItemFromTE(worldIn, player, handIn, heldItem, te))
					return InteractionResult.SUCCESS;

				if (EmptyingByBasin.canItemBeEmptied(worldIn, heldItem)
					|| GenericItemFilling.canItemBeFilled(worldIn, heldItem))
					return InteractionResult.SUCCESS;
				if (heldItem.getItem()
					.equals(Items.SPONGE)
					&& !te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
						.map(iFluidHandler -> iFluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE))
						.orElse(FluidStack.EMPTY)
						.isEmpty()) {
					return InteractionResult.SUCCESS;
				}
				return InteractionResult.PASS;
			}

			IItemHandlerModifiable inv = te.itemCapability.orElse(new ItemStackHandler(1));
			boolean success = false;
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemStack stackInSlot = inv.getStackInSlot(slot);
				if (stackInSlot.isEmpty())
					continue;
				player.getInventory()
					.placeItemBackInInventory(stackInSlot);
				inv.setStackInSlot(slot, ItemStack.EMPTY);
				success = true;
			}
			if (success)
				worldIn.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f,
					1f + Create.RANDOM.nextFloat());
			te.onEmptied();
			return InteractionResult.SUCCESS;
		});
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
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
				itemEntity.discard();
				return;
			}

			itemEntity.setItem(insertItem);
		});
	}

	@Override
	public VoxelShape getInteractionShape(BlockState p_199600_1_, BlockGetter p_199600_2_, BlockPos p_199600_3_) {
		return AllShapes.BASIN_RAYTRACE_SHAPE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.BASIN_BLOCK_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
		if (ctx instanceof EntityCollisionContext && ((EntityCollisionContext) ctx).getEntity() instanceof ItemEntity)
			return AllShapes.BASIN_COLLISION_SHAPE;
		return getShape(state, reader, pos, ctx);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
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
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
		return getTileEntityOptional(worldIn, pos).map(BasinTileEntity::getInputInventory)
			.map(ItemHelper::calcRedstoneFromInventory)
			.orElse(0);
	}

	@Override
	public Class<BasinTileEntity> getTileEntityClass() {
		return BasinTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends BasinTileEntity> getTileEntityType() {
		return AllTileEntities.BASIN.get();
	}

	public static boolean canOutputTo(BlockGetter world, BlockPos basinPos, Direction direction) {
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
			BlockEntity tileEntity = world.getBlockEntity(output);
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
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
