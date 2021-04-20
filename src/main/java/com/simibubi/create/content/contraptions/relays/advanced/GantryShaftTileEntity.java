package com.simibubi.create.content.contraptions.relays.advanced;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageTileEntity;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class GantryShaftTileEntity extends KineticTileEntity {

	public GantryShaftTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);

		if (!canAssembleOn())
			return;
		for (Direction d : Iterate.directions) {
			if (d.getAxis() == getBlockState().get(GantryShaftBlock.FACING)
				.getAxis())
				continue;
			BlockPos offset = pos.offset(d);
			BlockState pinionState = world.getBlockState(offset);
			if (!AllBlocks.GANTRY_CARRIAGE.has(pinionState))
				continue;
			if (pinionState.get(GantryCarriageBlock.FACING) != d)
				continue;
			TileEntity tileEntity = world.getTileEntity(offset);
			if (tileEntity instanceof GantryCarriageTileEntity)
				((GantryCarriageTileEntity) tileEntity).queueAssembly();
		}

	}

	@Override
	public float propagateRotationTo(KineticTileEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
		boolean connectedViaAxes, boolean connectedViaCogs) {
		float defaultModifier =
			super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);

		if (connectedViaAxes)
			return defaultModifier;
		if (!stateFrom.get(GantryShaftBlock.POWERED))
			return defaultModifier;
		if (!AllBlocks.GANTRY_CARRIAGE.has(stateTo))
			return defaultModifier;

		Direction direction = Direction.getFacingFromVector(diff.getX(), diff.getY(), diff.getZ());
		if (stateTo.get(GantryCarriageBlock.FACING) != direction)
			return defaultModifier;
		return GantryCarriageTileEntity.getGantryPinionModifier(stateFrom.get(GantryShaftBlock.FACING),
			stateTo.get(GantryCarriageBlock.FACING));
	}

	@Override
	public boolean isCustomConnection(KineticTileEntity other, BlockState state, BlockState otherState) {
		if (!AllBlocks.GANTRY_CARRIAGE.has(otherState))
			return false;
		final BlockPos diff = other.getPos()
			.subtract(pos);
		Direction direction = Direction.getFacingFromVector(diff.getX(), diff.getY(), diff.getZ());
		return otherState.get(GantryCarriageBlock.FACING) == direction;
	}

	public boolean canAssembleOn() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.GANTRY_SHAFT.has(blockState))
			return false;
		if (blockState.get(GantryShaftBlock.POWERED))
			return false;
		float speed = getPinionMovementSpeed();

		switch (blockState.get(GantryShaftBlock.PART)) {
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
		return MathHelper.clamp(-getSpeed() / 512f, -.49f, .49f);
	}
	
	@Override
	protected boolean isNoisy() {
		return false;
	}

}
