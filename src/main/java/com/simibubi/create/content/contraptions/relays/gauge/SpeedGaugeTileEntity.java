package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.List;

import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class SpeedGaugeTileEntity extends GaugeTileEntity {

	public SpeedGaugeTileEntity(TileEntityType<? extends SpeedGaugeTileEntity> type) {
		super(type);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		float speed = Math.abs(getSpeed());

		color = speed == 0 ? 0x333333
			: Color.mixColors(SpeedLevel.of(speed)
				.getColor(), 0xffffff, .25f);
		if (speed == 69)
			AllTriggers.triggerForNearbyPlayers(AllTriggers.SPEED_READ, level, worldPosition, 6, GogglesItem::canSeeParticles);

		dialTarget = getDialTarget(speed);
		setChanged();
	}

	public static float getDialTarget(float speed) {
		speed = Math.abs(speed);
		float medium = AllConfigs.SERVER.kinetics.mediumSpeed.get()
			.floatValue();
		float fast = AllConfigs.SERVER.kinetics.fastSpeed.get()
			.floatValue();
		float max = AllConfigs.SERVER.kinetics.maxRotationSpeed.get()
			.floatValue();
		float target = 0;
		if (speed == 0)
			target = 0;
		else if (speed < medium)
			target = MathHelper.lerp(speed / medium, 0, .45f);
		else if (speed < fast)
			target = MathHelper.lerp((speed - medium) / (fast - medium), .45f, .75f);
		else
			target = MathHelper.lerp((speed - fast) / (max - fast), .75f, 1.125f);
		return target;
	}

	@Override
	public boolean addToGoggleTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);

		tooltip.add(componentSpacing.plainCopy().append(Lang.translate("gui.speedometer.title").withStyle(TextFormatting.GRAY)));
		tooltip.add(componentSpacing.plainCopy().append(SpeedLevel.getFormattedSpeedText(speed, isOverStressed())));

		return true;
	}
}
