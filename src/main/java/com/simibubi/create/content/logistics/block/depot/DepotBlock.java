package com.simibubi.create.content.logistics.block.depot;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DepotBlock extends Block implements ITE<DepotTileEntity>, IWrenchable {

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
		return SharedDepotBlockMethods.onUse(state, world, pos, player, hand, ray);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		SharedDepotBlockMethods.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		SharedDepotBlockMethods.onLanded(worldIn, entityIn);
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		return SharedDepotBlockMethods.getComparatorInputOverride(blockState, worldIn, pos);
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
