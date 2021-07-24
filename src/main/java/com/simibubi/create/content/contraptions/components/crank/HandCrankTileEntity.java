package com.simibubi.create.content.contraptions.components.crank;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HandCrankTileEntity extends GeneratingKineticTileEntity {

	public int inUse;
	public boolean backwards;
	public float independentAngle;
	public float chasingVelocity;

	public HandCrankTileEntity(TileEntityType<? extends HandCrankTileEntity> type) {
		super(type);
	}

	public void turn(boolean back) {
		boolean update = false;

		if (getGeneratedSpeed() == 0 || back != backwards)
			update = true;

		inUse = 10;
		this.backwards = back;
		if (update && !level.isClientSide)
			updateGeneratedRotation();
	}

	@Override
	public float getGeneratedSpeed() {
		Block block = getBlockState().getBlock();
		if (!(block instanceof HandCrankBlock))
			return 0;
		HandCrankBlock crank = (HandCrankBlock) block;
		int speed = (inUse == 0 ? 0 : backwards ? -1 : 1) * crank.getRotationSpeed();
		return convertToDirection(speed, getBlockState().getValue(HandCrankBlock.FACING));
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putInt("InUse", inUse);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		inUse = compound.getInt("InUse");
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();

		float actualSpeed = getSpeed();
		chasingVelocity += ((actualSpeed * 10 / 3f) - chasingVelocity) * .25f;
		independentAngle += chasingVelocity;

		if (inUse > 0) {
			inUse--;

			if (inUse == 0 && !level.isClientSide)
				updateGeneratedRotation();
		}
	}

	@Override
	protected Block getStressConfigKey() {
		return AllBlocks.HAND_CRANK.get();
	}

	@Override
	public boolean shouldRenderNormally() {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void tickAudio() {
		super.tickAudio();
		if (inUse > 0 && AnimationTickHolder.getTicks() % 10 == 0) {
			if (!AllBlocks.HAND_CRANK.has(getBlockState()))
				return;
			AllSoundEvents.CRANKING.playAt(level, worldPosition, (inUse) / 2.5f, .65f + (10 - inUse) / 10f, true);
		}
	}

}
