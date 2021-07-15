package com.simibubi.create.content.logistics.item;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class LecternControllerBlock extends LecternBlock implements ITE<LecternControllerTileEntity>, ISpecialBlockItemRequirement {

	public LecternControllerBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(HAS_BOOK, true));
	}

	@Nullable
	@Override
	public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
		return null;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.LECTERN_CONTROLLER.create();
	}

	@Override
	public Class<LecternControllerTileEntity> getTileEntityClass() {
		return LecternControllerTileEntity.class;
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if (!player.isShiftKeyDown() && LecternControllerTileEntity.playerInRange(player, world, pos)) {
			if (!world.isClientSide)
				withTileEntityDo(world, pos, te -> te.tryStartUsing(player));
			return ActionResultType.SUCCESS;
		}

		if (player.isShiftKeyDown()) {
			if (!world.isClientSide)
				replaceWithLectern(state, world, pos);
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (!world.isClientSide)
				withTileEntityDo(world, pos, te -> te.dropController(state));

			super.onRemove(state, world, pos, newState, isMoving);
		}
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos) {
		return 15;
	}

	public void replaceLectern(BlockState lecternState, World world, BlockPos pos, ItemStack controller) {
		world.setBlockAndUpdate(pos, defaultBlockState()
			.setValue(FACING, lecternState.getValue(FACING))
			.setValue(POWERED, lecternState.getValue(POWERED)));
		withTileEntityDo(world, pos, te -> te.setController(controller));
	}

	public void replaceWithLectern(BlockState state, World world, BlockPos pos) {
		AllSoundEvents.CONTROLLER_TAKE.playOnServer(world, pos);
		world.setBlockAndUpdate(pos, Blocks.LECTERN.defaultBlockState()
			.setValue(FACING, state.getValue(FACING))
			.setValue(POWERED, state.getValue(POWERED)));
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return Blocks.LECTERN.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, TileEntity te) {
		ArrayList<ItemStack> requiredItems = new ArrayList<>();
		requiredItems.add(new ItemStack(Blocks.LECTERN));
		requiredItems.add(new ItemStack(AllItems.LINKED_CONTROLLER.get()));
		return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, requiredItems);
	}
}
