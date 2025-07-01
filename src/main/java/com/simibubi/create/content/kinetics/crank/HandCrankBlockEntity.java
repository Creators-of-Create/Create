package com.simibubi.create.content.kinetics.crank;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HandCrankBlockEntity extends GeneratingKineticBlockEntity {

	public int inUse;
	public boolean backwards;
	/**
	 * In degrees
	 */
	public float independentAngle;
	public float chasingAngularVelocity;

	public HandCrankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
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

	/**
	 * In degrees
	 */
	public float getIndependentAngle(float partialTicks) {
		return independentAngle + partialTicks * chasingAngularVelocity;
	}

	@Override
	public float getGeneratedSpeed() {
		Block block = getBlockState().getBlock();
		if (!(block instanceof HandCrankBlock crank))
			return 0;
		int speed = (inUse == 0 ? 0 : clockwise() ? -1 : 1) * crank.getRotationSpeed();
		return convertToDirection(speed, getBlockState().getValue(HandCrankBlock.FACING));
	}

	protected boolean clockwise() {
		return backwards;
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("InUse", inUse);
		compound.putBoolean("Backwards", backwards);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		inUse = compound.getInt("InUse");
		backwards = compound.getBoolean("Backwards");
		super.read(compound, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();

		float actualAngularSpeed = KineticBlockEntity.convertToAngular(getSpeed());
		chasingAngularVelocity += (actualAngularSpeed - chasingAngularVelocity) / 4f;
		independentAngle += chasingAngularVelocity;

		if (inUse > 0) {
			inUse--;

			if (inUse == 0 && !level.isClientSide) {
				sequenceContext = null;
				updateGeneratedRotation();
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public SuperByteBuffer getRenderedHandle() {
		BlockState blockState = getBlockState();
		Direction facing = blockState.getOptionalValue(HandCrankBlock.FACING)
			.orElse(Direction.UP);
		return CachedBuffers.partialFacing(AllPartialModels.HAND_CRANK_HANDLE, blockState, facing.getOpposite());
	}

	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderShaft() {
		return true;
	}

	@Override
	protected Block getStressConfigKey() {
		return AllBlocks.HAND_CRANK.has(getBlockState()) ? AllBlocks.HAND_CRANK.get()
			: AllBlocks.COPPER_VALVE_HANDLE.get();
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
