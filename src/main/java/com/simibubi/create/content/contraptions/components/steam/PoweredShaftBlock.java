package com.simibubi.create.content.contraptions.components.steam;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.elementary.AbstractShaftBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PoweredShaftBlock extends AbstractShaftBlock {

	public PoweredShaftBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.EIGHT_VOXEL_POLE.get(pState.getValue(AXIS));
	}

	@Override
	public BlockEntityType<? extends KineticTileEntity> getTileEntityType() {
		return AllTileEntities.POWERED_SHAFT.get();
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
		if (!stillValid(pState, pLevel, pPos))
			pLevel.setBlock(pPos, AllBlocks.SHAFT.getDefaultState()
				.setValue(ShaftBlock.AXIS, pState.getValue(AXIS))
				.setValue(WATERLOGGED, pState.getValue(WATERLOGGED)), 3);
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
		return AllBlocks.SHAFT.asStack();
	}
	
	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		return stillValid(pState, pLevel, pPos);
	}
	
	public static boolean stillValid(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		for (Direction d : Iterate.directions) {
			if (d.getAxis() == pState.getValue(AXIS))
				continue;
			BlockState engineState = pLevel.getBlockState(pPos.relative(d, 2));
			if (!(engineState.getBlock()instanceof SteamEngineBlock engine))
				continue;
			if (SteamEngineBlock.isShaftValid(engineState, pState))
				return true;
		}
		return false;
	}

	public static BlockState getEquivalent(BlockState stateForPlacement) {
		return AllBlocks.POWERED_SHAFT.getDefaultState()
			.setValue(PoweredShaftBlock.AXIS, stateForPlacement.getValue(ShaftBlock.AXIS))
			.setValue(WATERLOGGED, stateForPlacement.getValue(WATERLOGGED));
	}

}
