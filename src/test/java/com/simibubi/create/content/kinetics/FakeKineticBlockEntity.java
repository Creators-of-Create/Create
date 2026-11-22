package com.simibubi.create.content.kinetics;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lightweight fake of a kinetic block entity. Stress values are fixed instead of being read from the config
 * and network updates are only recorded, so the network logic can run without a bootstrapped game.
 */
public class FakeKineticBlockEntity extends KineticBlockEntity {

	private final float generatedSpeed;
	private final float stressImpact;
	private final float stressCapacity;

	public float receivedCapacity;
	public float receivedStress;
	public int receivedNetworkSize;

	public FakeKineticBlockEntity(BlockPos pos, float speed, float generatedSpeed, float stressImpact,
		float stressCapacity) {
		super(null, pos, null);
		this.speed = speed;
		this.generatedSpeed = generatedSpeed;
		this.stressImpact = stressImpact;
		this.stressCapacity = stressCapacity;
	}

	@Override
	public boolean isValidBlockState(BlockState state) {
		return true;
	}

	@Override
	public float getGeneratedSpeed() {
		return generatedSpeed;
	}

	@Override
	public float calculateStressApplied() {
		return stressImpact;
	}

	@Override
	public float calculateAddedStressCapacity() {
		return stressCapacity;
	}

	@Override
	public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
		networkDirty = false;
		receivedCapacity = maxStress;
		receivedStress = currentStress;
		receivedNetworkSize = networkSize;
	}
}
