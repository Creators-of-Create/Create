package com.simibubi.create.content.contraptions.relays.advanced;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageTileEntity;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class GantryShaftTileEntity extends KineticTileEntity {

	public GantryShaftTileEntity(BlockEntityType<?> typeIn) {
		super(typeIn);
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

	@Override
	public float propagateRotationTo(KineticTileEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
		boolean connectedViaAxes, boolean connectedViaCogs) {
		float defaultModifier =
			super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);

		if (connectedViaAxes)
			return defaultModifier;
		if (!stateFrom.getValue(GantryShaftBlock.POWERED))
			return defaultModifier;
		if (!AllBlocks.GANTRY_CARRIAGE.has(stateTo))
			return defaultModifier;

		Direction direction = Direction.getNearest(diff.getX(), diff.getY(), diff.getZ());
		if (stateTo.getValue(GantryCarriageBlock.FACING) != direction)
			return defaultModifier;
		return GantryCarriageTileEntity.getGantryPinionModifier(stateFrom.getValue(GantryShaftBlock.FACING),
			stateTo.getValue(GantryCarriageBlock.FACING));
	}

	@Override
	public boolean isCustomConnection(KineticTileEntity other, BlockState state, BlockState otherState) {
		if (!AllBlocks.GANTRY_CARRIAGE.has(otherState))
			return false;
		final BlockPos diff = other.getBlockPos()
			.subtract(worldPosition);
		Direction direction = Direction.getNearest(diff.getX(), diff.getY(), diff.getZ());
		return otherState.getValue(GantryCarriageBlock.FACING) == direction;
	}

	public boolean canAssembleOn() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.GANTRY_SHAFT.has(blockState))
			return false;
		if (blockState.getValue(GantryShaftBlock.POWERED))
			return false;
		float speed = getPinionMovementSpeed();

		switch (blockState.getValue(GantryShaftBlock.PART)) {
		case END:
			return speed < 0;
		case MIDDLE:
			return speed != 0;
		case START:
			return speed > 0;
		case SINGLE:
		default:
			return false;
		}
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
