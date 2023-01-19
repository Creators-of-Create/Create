package com.simibubi.create.content.contraptions.components.saw;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.components.actors.DrillBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SawBlock extends DirectionalAxisKineticBlock implements IBE<SawBlockEntity> {
	public static DamageSource damageSourceSaw = new DamageSource("create.mechanical_saw").bypassArmor();

	public SawBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState stateForPlacement = super.getStateForPlacement(context);
		Direction facing = stateForPlacement.getValue(FACING);
		if (facing.getAxis().isVertical())
			return stateForPlacement;
		return stateForPlacement.setValue(AXIS_ALONG_FIRST_COORDINATE, facing.getAxis() == Axis.X);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.CASING_12PX.get(state.getValue(FACING));
	}

	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		if (entityIn instanceof ItemEntity)
			return;
		if (!new AABB(pos).deflate(.1f).intersects(entityIn.getBoundingBox()))
			return;
		withBlockEntityDo(worldIn, pos, be -> {
			if (be.getSpeed() == 0)
				return;
			entityIn.hurt(damageSourceSaw, (float) DrillBlock.getDamage(be.getSpeed()));
		});
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);
		if (!(entityIn instanceof ItemEntity))
			return;
		if (entityIn.level.isClientSide)
			return;

		BlockPos pos = entityIn.blockPosition();
		withBlockEntityDo(entityIn.level, pos, be -> {
			if (be.getSpeed() == 0)
				return;
			be.insertItem((ItemEntity) entityIn);
		});
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	public static boolean isHorizontal(BlockState state) {
		return state.getValue(FACING).getAxis().isHorizontal();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return isHorizontal(state) ? state.getValue(FACING).getAxis() : super.getRotationAxis(state);
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return isHorizontal(state) ? face == state.getValue(FACING).getOpposite()
				: super.hasShaftTowards(world, pos, state, face);
	}

	@Override
	public Class<SawBlockEntity> getBlockEntityClass() {
		return SawBlockEntity.class;
	}
	
	@Override
	public BlockEntityType<? extends SawBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.SAW.get();
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
