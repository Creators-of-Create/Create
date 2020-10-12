package com.simibubi.create.content.logistics.block.depot;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.items.ItemStackHandler;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DepotBlock extends Block implements ITE<DepotTileEntity> {

	public DepotBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.DEPOT;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.DEPOT.create();
	}

	@Override
	public Class<DepotTileEntity> getTileEntityClass() {
		return DepotTileEntity.class;
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult ray) {
		if (ray.getFace() != Direction.UP)
			return ActionResultType.PASS;
		if (world.isRemote)
			return ActionResultType.SUCCESS;

		withTileEntityDo(world, pos, te -> {
			ItemStack heldItem = player.getHeldItem(hand);
			boolean wasEmptyHanded = heldItem.isEmpty();
			boolean shouldntPlaceItem = AllBlocks.MECHANICAL_ARM.isIn(heldItem);

			ItemStack mainItemStack = te.getHeldItemStack();
			if (!mainItemStack.isEmpty()) {
				player.inventory.placeItemBackInInventory(world, mainItemStack);
				te.setHeldItem(null);
			}
			ItemStackHandler outputs = te.processingOutputBuffer;
			for (int i = 0; i < outputs.getSlots(); i++)
				player.inventory.placeItemBackInInventory(world, outputs.extractItem(i, 64, false));

			if (!wasEmptyHanded && !shouldntPlaceItem) {
				TransportedItemStack transported = new TransportedItemStack(heldItem);
				transported.insertedFrom = player.getHorizontalFacing();
				transported.prevBeltPosition = .25f;
				transported.beltPosition = .25f;
				te.setHeldItem(transported);
				player.setHeldItem(hand, ItemStack.EMPTY);
			}

			te.markDirty();
			te.sendData();
		});

		return ActionResultType.SUCCESS;
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.getBlock() == newState.getBlock()) {
			return;
		}

		withTileEntityDo(worldIn, pos, te -> {
			ItemHelper.dropContents(worldIn, pos, te.processingOutputBuffer);
			if (!te.getHeldItemStack()
				.isEmpty())
				InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), te.getHeldItemStack());
		});
		worldIn.removeTileEntity(pos);
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		if (!AllBlocks.DEPOT.has(worldIn.getBlockState(entityIn.getPosition())))
			return;
		if (!(entityIn instanceof ItemEntity))
			return;
		if (!entityIn.isAlive())
			return;
		if (entityIn.world.isRemote)
			return;
		ItemEntity itemEntity = (ItemEntity) entityIn;
		DirectBeltInputBehaviour inputBehaviour =
			TileEntityBehaviour.get(worldIn, entityIn.getPosition(), DirectBeltInputBehaviour.TYPE);
		if (inputBehaviour == null)
			return;
		ItemStack remainder = inputBehaviour.handleInsertion(itemEntity.getItem(), Direction.DOWN, false);
		itemEntity.setItem(remainder);
		if (remainder.isEmpty())
			itemEntity.remove();
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		try {
			return ItemHelper.calcRedstoneFromInventory(getTileEntity(worldIn, pos).itemHandler);
		} catch (TileEntityException ignored) {
		}
		return 0;
	}

}
