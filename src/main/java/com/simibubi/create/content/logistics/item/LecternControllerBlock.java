package com.simibubi.create.content.logistics.item;

import javax.annotation.Nullable;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class LecternControllerBlock extends LecternBlock implements ITE<LecternControllerTileEntity> {

	public LecternControllerBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(HAS_BOOK, true));
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader p_196283_1_) {
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
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if (!player.isSneaking() && LecternControllerTileEntity.playerInRange(player, world, pos)) {
			if (!world.isRemote)
				withTileEntityDo(world, pos, te -> te.tryStartUsing(player));
			return ActionResultType.SUCCESS;
		}

		if (player.isSneaking()) {
			if (!world.isRemote)
				replaceWithLectern(state, world, pos);
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.isIn(newState.getBlock())) {
			if (!world.isRemote)
				withTileEntityDo(world, pos, te -> te.dropController(state));

			super.onReplaced(state, world, pos, newState, isMoving);
		}
	}

	@Override
	public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
		return 15;
	}

	public void replaceLectern(BlockState lecternState, World world, BlockPos pos, ItemStack controller) {
		world.setBlockState(pos, getDefaultState()
			.with(FACING, lecternState.get(FACING))
			.with(POWERED, lecternState.get(POWERED)));
		withTileEntityDo(world, pos, te -> te.setController(controller));
	}

	public void replaceWithLectern(BlockState state, World world, BlockPos pos) {
		AllSoundEvents.CONTROLLER_TAKE.playOnServer(world, pos);
		world.setBlockState(pos, Blocks.LECTERN.getDefaultState()
			.with(FACING, state.get(FACING))
			.with(POWERED, state.get(POWERED)));
	}

}
