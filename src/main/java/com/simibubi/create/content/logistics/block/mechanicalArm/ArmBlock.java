package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ArmBlock extends KineticBlock implements ITE<ArmTileEntity> {

	public ArmBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean hasIntegratedCogwheel(IWorldReader world, BlockPos pos, BlockState state) {
		return true;
	}
	
	@Override
	public ActionResultType onUse(BlockState p_225533_1_, World p_225533_2_, BlockPos p_225533_3_,
		PlayerEntity p_225533_4_, Hand p_225533_5_, BlockRayTraceResult p_225533_6_) {
		withTileEntityDo(p_225533_2_, p_225533_3_, ArmTileEntity::toggleRave);
		return ActionResultType.SUCCESS;
	}
	
	
	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.MECHANICAL_ARM;
	}
	
	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.MECHANICAL_ARM.create();
	}

	@Override
	public Class<ArmTileEntity> getTileEntityClass() {
		return ArmTileEntity.class;
	}

}
