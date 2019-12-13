package com.simibubi.create.modules.contraptions.base;

import java.util.Optional;
import java.util.UUID;

import com.simibubi.create.modules.contraptions.base.IRotate.SpeedLevel;

import net.minecraft.tileentity.TileEntityType;

public abstract class GeneratingKineticTileEntity extends KineticTileEntity {

	public GeneratingKineticTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
	}

	protected void notifyStressCapacityChange(float capacity) {
		getNetwork().updateCapacityFor(this, capacity);
	}

	@Override
	public void reActivateSource() {
		updateGeneratedRotation();
	}

	public void updateGeneratedRotation() {
		float speed = getGeneratedSpeed();
		float prevSpeed = this.speed;

		if (this.speed != speed) {

			if (!world.isRemote) {
				SpeedLevel levelBefore = SpeedLevel.of(this.speed);
				SpeedLevel levelafter = SpeedLevel.of(speed);
				if (levelBefore != levelafter)
					queueRotationIndicators();
			}

			if (speed == 0) {
				if (hasSource())
					notifyStressCapacityChange(0);
				else {
					detachKinetics();
					setSpeed(speed);
					newNetworkID = null;
					updateNetwork = true;
				}
			} else if (this.speed == 0) {
				setSpeed(speed);
				newNetworkID = UUID.randomUUID();
				updateNetwork = true;
				attachKinetics();
			} else {
				if (hasSource()) {
					if (Math.abs(this.speed) >= Math.abs(speed)) {
						if (Math.signum(this.speed) == Math.signum(speed))
							notifyStressCapacityChange(getAddedStressCapacity());
						else
							world.destroyBlock(pos, true);
					} else {
						detachKinetics();
						setSpeed(speed);
						source = Optional.empty();
						newNetworkID = UUID.randomUUID();
						updateNetwork = true;
						attachKinetics();
					}
				} else {
					detachKinetics();
					setSpeed(speed);
					attachKinetics();
				}
			}
		}

		if (hasNetwork() && speed != 0) {
			getNetwork().updateCapacityFor(this, getAddedStressCapacity());
			getNetwork().updateStressCapacity();
			getNetwork().updateStress();
		}

		onSpeedChanged(prevSpeed);
		sendData();
	}

}
