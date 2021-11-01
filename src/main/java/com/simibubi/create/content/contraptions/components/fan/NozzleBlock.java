package com.simibubi.create.content.contraptions.components.fan;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NozzleBlock extends WrenchableDirectionalBlock {

	public NozzleBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.FAIL;
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return AllTileEntities.NOZZLE.create();
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getClickedFace());
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.NOZZLE.get(state.getValue(FACING));
	}
	
	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		if (fromPos.equals(pos.relative(state.getValue(FACING).getOpposite())))
			if (!canSurvive(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		Direction towardsFan = state.getValue(FACING).getOpposite();
		BlockEntity te = worldIn.getBlockEntity(pos.relative(towardsFan));
		return te instanceof IAirCurrentSource
				&& ((IAirCurrentSource) te).getAirflowOriginSide() == towardsFan.getOpposite();
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
