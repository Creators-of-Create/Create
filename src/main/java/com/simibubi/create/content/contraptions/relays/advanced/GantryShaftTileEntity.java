package com.simibubi.create.content.contraptions.relays.advanced;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageTileEntity;
import com.simibubi.create.content.contraptions.solver.AllConnections;
import com.simibubi.create.content.contraptions.solver.ConnectionsBuilder;
import com.simibubi.create.content.contraptions.solver.KineticConnections;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GantryShaftTileEntity extends KineticTileEntity {

	public GantryShaftTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	private KineticConnections connections;

	public void updateConnections(BlockState state) {
		if (!AllBlocks.GANTRY_SHAFT.has(state)) {
			connections = KineticConnections.empty();
			return;
		}

		Direction facing = state.getValue(GantryShaftBlock.FACING);
		ConnectionsBuilder builder = ConnectionsBuilder.builder().withFullShaft(facing.getAxis());

		if (state.getValue(GantryShaftBlock.POWERED))
			builder = builder.withDirectional(AllConnections.Directional.GANTRY_RACK, facing);

		connections = builder.build();
	}

	@Override
	public KineticConnections getConnections() {
		return connections;
	}

	@Override
	public void initialize() {
		updateConnections(getBlockState());
		super.initialize();
	}

	public void checkAttachedCarriageBlocks() {
		if (!canAssembleOn())
			return;
		for (Direction d : Iterate.directions) {
			if (d.getAxis() == getBlockState().getValue(GantryShaftBlock.FACING)
					.getAxis())
				continue;
			BlockPos offset = worldPosition.relative(d);
			BlockState pinionState = level.getBlockState(offset);
			if (!AllBlocks.GANTRY_CARRIAGE.has(pinionState))
				continue;
			if (pinionState.getValue(GantryCarriageBlock.FACING) != d)
				continue;
			BlockEntity tileEntity = level.getBlockEntity(offset);
			if (tileEntity instanceof GantryCarriageTileEntity)
				((GantryCarriageTileEntity) tileEntity).queueAssembly();
		}
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
		checkAttachedCarriageBlocks();
	}

	public boolean canAssembleOn() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.GANTRY_SHAFT.has(blockState))
			return false;
		if (blockState.getValue(GantryShaftBlock.POWERED))
			return false;
		float speed = getPinionMovementSpeed();

		return switch (blockState.getValue(GantryShaftBlock.PART)) {
			case END -> speed < 0;
			case MIDDLE -> speed != 0;
			case START -> speed > 0;
			default -> false;
		};
	}

	public float getPinionMovementSpeed() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.GANTRY_SHAFT.has(blockState))
			return 0;
		return Mth.clamp(convertToLinear(-getSpeed()), -.49f, .49f);
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

}
