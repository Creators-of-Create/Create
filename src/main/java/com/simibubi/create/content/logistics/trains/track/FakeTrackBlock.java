package com.simibubi.create.content.logistics.trains.track;

import java.util.Random;

import com.simibubi.create.AllTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FakeTrackBlock extends Block implements EntityBlock {

	public FakeTrackBlock(Properties p_49795_) {
		super(p_49795_.randomTicks()
			.noCollission()
			.noOcclusion());
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return Shapes.empty();
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
		if (pLevel.getBlockEntity(pPos) instanceof FakeTrackTileEntity te)
			te.randomTick();
	}

	public static void keepAlive(LevelAccessor level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof FakeTrackTileEntity te)
			te.keepAlive();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return AllTileEntities.FAKE_TRACK.create(pPos, pState);
	}

}
