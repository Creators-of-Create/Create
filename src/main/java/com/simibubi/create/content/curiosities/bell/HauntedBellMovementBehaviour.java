package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.content.contraptions.components.actors.BellMovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.util.math.BlockPos;

public class HauntedBellMovementBehaviour extends BellMovementBehaviour {

	public static final int DISTANCE = 3;

	@Override
	public void tick(MovementContext context) {
		int recharge = getRecharge(context);
		if (recharge > 0)
			setRecharge(context, recharge - 1);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		if (!context.world.isRemote && getRecharge(context) == 0) {
			HauntedBellPulser.sendPulse(context.world, pos, DISTANCE, false);
			setRecharge(context, HauntedBellTileEntity.RECHARGE_TICKS);
			playSound(context);
		}
	}

	@Override
	public void writeExtraData(MovementContext context) {
		context.tileData.putInt("Recharge", getRecharge(context));
	}

	private int getRecharge(MovementContext context) {
		if (!(context.temporaryData instanceof Integer) && context.world != null) {
			context.temporaryData = context.tileData.getInt("Recharge");
		}
		return (Integer) context.temporaryData;
	}

	private void setRecharge(MovementContext context, int value) {
		context.temporaryData = value;
	}

}
