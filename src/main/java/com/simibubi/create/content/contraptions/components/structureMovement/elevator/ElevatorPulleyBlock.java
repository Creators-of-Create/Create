package com.simibubi.create.content.contraptions.components.structureMovement.elevator;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ElevatorPulleyBlock extends HorizontalKineticBlock implements IBE<ElevatorPulleyBlockEntity> {

	public ElevatorPulleyBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (!player.mayBuild())
			return InteractionResult.FAIL;
		if (player.isShiftKeyDown())
			return InteractionResult.FAIL;
		if (!player.getItemInHand(handIn)
			.isEmpty())
			return InteractionResult.PASS;
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;
		return onBlockEntityUse(worldIn, pos, be -> {
			be.clicked();
			return InteractionResult.SUCCESS;
		});
	}

	@Override
	public BlockEntityType<? extends ElevatorPulleyBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.ELEVATOR_PULLEY.get();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING)
			.getClockWise()
			.getAxis();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.ELEVATOR_PULLEY.get(state.getValue(HORIZONTAL_FACING));
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return getRotationAxis(state) == face.getAxis();
	}

	@Override
	public Class<ElevatorPulleyBlockEntity> getBlockEntityClass() {
		return ElevatorPulleyBlockEntity.class;
	}

}
