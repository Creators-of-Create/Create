package com.simibubi.create.modules.schematics.block;

import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.AllShapes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class SchematicannonBlock extends Block implements ITE<SchematicannonTileEntity> {

	public SchematicannonBlock() {
		super(Properties.from(Blocks.DISPENSER));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SchematicannonTileEntity();
	}

	@Override
	public boolean isSolid(BlockState state) {
		return false;
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.SCHEMATICANNON_SHAPE;
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
		withTileEntityDo(world, pos, SchematicannonTileEntity::findInventories);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (worldIn.isRemote)
			return true;

		withTileEntityDo(worldIn, pos,
				te -> NetworkHooks.openGui((ServerPlayerEntity) player, te, te::sendToContainer));
		return true;
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.getBlock() == newState.getBlock())
			return;

		withTileEntityDo(worldIn, pos, te -> ItemHelper.dropContents(worldIn, pos, te.inventory));
		worldIn.removeTileEntity(pos);
	}

	@Override
	public Class<SchematicannonTileEntity> getTileEntityClass() {
		return SchematicannonTileEntity.class;
	}

}
