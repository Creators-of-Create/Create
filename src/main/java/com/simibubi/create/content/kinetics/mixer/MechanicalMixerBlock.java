package com.simibubi.create.content.kinetics.mixer;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MechanicalMixerBlock extends KineticBlock implements IBE<MechanicalMixerBlockEntity>, ICogWheel {

	public MechanicalMixerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return !BasinBlock.isBasin(worldIn, pos.below());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext
			&& ((EntityCollisionContext) context).getEntity() instanceof Player)
			return AllShapes.CASING_14PX.get(Direction.DOWN);

		return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return false;
	}

	@Override
	public float getParticleTargetRadius() {
		return .85f;
	}

	@Override
	public float getParticleInitialRadius() {
		return .75f;
	}

	@Override
	public SpeedLevel getMinimumRequiredSpeedLevel() {
		return SpeedLevel.MEDIUM;
	}

	@Override
	public Class<MechanicalMixerBlockEntity> getBlockEntityClass() {
		return MechanicalMixerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends MechanicalMixerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.MECHANICAL_MIXER.get();
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
