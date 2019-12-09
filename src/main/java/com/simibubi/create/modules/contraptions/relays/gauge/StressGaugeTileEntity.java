package com.simibubi.create.modules.contraptions.relays.gauge;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.ColorHelper;

public class StressGaugeTileEntity extends GaugeTileEntity {

	public StressGaugeTileEntity() {
		super(AllTileEntities.STRESS_GAUGE.type);
	}

	@Override
	public void sync(float maxStress, float currentStress) {
		super.sync(maxStress, currentStress);

		if (overStressed)
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
	}

}
