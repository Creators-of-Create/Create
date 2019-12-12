package com.simibubi.create.modules.contraptions.relays.gauge;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.modules.contraptions.base.IRotate.SpeedLevel;

import net.minecraft.util.math.MathHelper;

public class SpeedGaugeTileEntity extends GaugeTileEntity {

	public SpeedGaugeTileEntity() {
		super(AllTileEntities.SPEED_GAUGE.type);
	}

	@Override
	public void onSpeedChanged() {
		super.onSpeedChanged();
		float speed = Math.abs(getSpeed());
		float medium = CreateConfig.parameters.mediumSpeed.get().floatValue();
		float fast = CreateConfig.parameters.fastSpeed.get().floatValue();
		float max = CreateConfig.parameters.maxRotationSpeed.get().floatValue();
		color = ColorHelper.mixColors(SpeedLevel.of(speed).getColor(), 0xffffff, .25f);

		if (speed == 0) {
			dialTarget = 0;
			color = 0x333333;
		} else if (speed < medium) {
			dialTarget = MathHelper.lerp(speed / medium, 0, .45f);
		} else if (speed < fast) {
			dialTarget = MathHelper.lerp((speed - medium) / (fast - medium), .45f, .75f);
		} else {
			dialTarget = MathHelper.lerp((speed - fast) / (max - fast), .75f, 1.125f);
		}
	}

}
