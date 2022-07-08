package com.simibubi.create.content.contraptions.components.flywheel;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlywheelBlock extends RotatedPillarKineticBlock implements ITE<FlywheelTileEntity> {

	public FlywheelBlock(Properties properties) {
		super(properties);
	}

	@Override
	public Class<FlywheelTileEntity> getTileEntityClass() {
		return FlywheelTileEntity.class;
	}
	
	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.LARGE_GEAR.get(pState.getValue(AXIS));
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockEntityType<? extends FlywheelTileEntity> getTileEntityType() {
		return AllTileEntities.FLYWHEEL.get();
	}
	
	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == getRotationAxis(state);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(AXIS);
	}

	@Override
	public float getParticleTargetRadius() {
		return 2f;
	}

	@Override
	public float getParticleInitialRadius() {
		return 1.75f;
	}
	
}
