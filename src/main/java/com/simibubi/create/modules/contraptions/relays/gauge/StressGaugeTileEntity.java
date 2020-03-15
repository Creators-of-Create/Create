package com.simibubi.create.modules.contraptions.relays.gauge;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.modules.contraptions.base.IRotate.StressImpact;

public class StressGaugeTileEntity extends GaugeTileEntity {

	public StressGaugeTileEntity() {
		super(AllTileEntities.STRESS_GAUGE.type);
	}

	@Override
	public void updateStressFromNetwork(float maxStress, float currentStress) {
		super.updateStressFromNetwork(maxStress, currentStress);

		if (!StressImpact.isEnabled())
			dialTarget = 0;
		else if (overStressed)
			dialTarget = 1.125f;
		else if (maxStress == 0)
			dialTarget = 0;
		else
			dialTarget = currentStress / maxStress;

		if (dialTarget > 0) {
			if (dialTarget < .5f)
				color = ColorHelper.mixColors(0x00FF00, 0xFFFF00, dialTarget * 2);
			else if (dialTarget < 1)
				color = ColorHelper.mixColors(0xFFFF00, 0xFF0000, (dialTarget) * 2 - 1);
			else
				color = 0xFF0000;
		}

		sendData();
		markDirty();
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		if (getSpeed() == 0) {
			dialTarget = 0;
			markDirty();
			return;
		}
		updateStressFromNetwork(capacity, stress);
	}

	public float getNetworkStress() {
		return stress;
	}

	public float getNetworkCapacity() {
		return capacity;
	}

}
