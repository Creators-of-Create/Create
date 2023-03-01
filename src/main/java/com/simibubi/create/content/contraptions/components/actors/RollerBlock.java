package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RollerBlock extends AttachedActorBlock implements IBE<RollerBlockEntity> {

	public static DamageSource damageSourceRoller = new DamageSource("create.mechanical_roller");

	public RollerBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withWater(defaultBlockState().setValue(FACING, context.getHorizontalDirection()
			.getOpposite()), context);
	}

	@Override
	public Class<RollerBlockEntity> getBlockEntityClass() {
		return RollerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends RollerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.MECHANICAL_ROLLER.get();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(FACING);
		return AllShapes.ROLLER_BASE.get(direction);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return true;
	}

}
