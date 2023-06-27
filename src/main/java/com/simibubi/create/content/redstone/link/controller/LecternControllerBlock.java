package com.simibubi.create.content.redstone.link.controller;

import java.util.ArrayList;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.schematics.requirement.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class LecternControllerBlock extends LecternBlock
	implements IBE<LecternControllerBlockEntity>, ISpecialBlockItemRequirement {

	public LecternControllerBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(HAS_BOOK, true));
	}

	@Override
	public Class<LecternControllerBlockEntity> getBlockEntityClass() {
		return LecternControllerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends LecternControllerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.LECTERN_CONTROLLER.get();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153573_, BlockState p_153574_) {
		return IBE.super.newBlockEntity(p_153573_, p_153574_);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		if (!player.isShiftKeyDown() && LecternControllerBlockEntity.playerInRange(player, world, pos)) {
			if (!world.isClientSide)
				withBlockEntityDo(world, pos, be -> be.tryStartUsing(player));
			return InteractionResult.SUCCESS;
		}

		if (player.isShiftKeyDown()) {
			if (!world.isClientSide)
				replaceWithLectern(state, world, pos);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (!world.isClientSide)
				withBlockEntityDo(world, pos, be -> be.dropController(state));

			super.onRemove(state, world, pos, newState, isMoving);
		}
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		return 15;
	}

	public void replaceLectern(BlockState lecternState, Level world, BlockPos pos, ItemStack controller) {
		world.setBlockAndUpdate(pos, defaultBlockState().setValue(FACING, lecternState.getValue(FACING))
			.setValue(POWERED, lecternState.getValue(POWERED)));
		withBlockEntityDo(world, pos, be -> be.setController(controller));
	}

	public void replaceWithLectern(BlockState state, Level world, BlockPos pos) {
		AllSoundEvents.CONTROLLER_TAKE.playOnServer(world, pos);
		world.setBlockAndUpdate(pos, Blocks.LECTERN.defaultBlockState()
			.setValue(FACING, state.getValue(FACING))
			.setValue(POWERED, state.getValue(POWERED)));
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
		return Blocks.LECTERN.getCloneItemStack(state, target, world, pos, player);
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
		ArrayList<ItemStack> requiredItems = new ArrayList<>();
		requiredItems.add(new ItemStack(Blocks.LECTERN));
		requiredItems.add(new ItemStack(AllItems.LINKED_CONTROLLER.get()));
		return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, requiredItems);
	}
}
