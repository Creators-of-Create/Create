package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.List;

import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SpeedGaugeBlockEntity extends GaugeBlockEntity {

	public SpeedGaugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		float speed = Math.abs(getSpeed());

		dialTarget = getDialTarget(speed);
		color = Color.mixColors(SpeedLevel.of(speed)
			.getColor(), 0xffffff, .25f);

		setChanged();
	}

	public static float getDialTarget(float speed) {
		speed = Math.abs(speed);
		float medium = AllConfigs.server().kinetics.mediumSpeed.get()
			.floatValue();
		float fast = AllConfigs.server().kinetics.fastSpeed.get()
			.floatValue();
		float max = AllConfigs.server().kinetics.maxRotationSpeed.get()
			.floatValue();
		float target = 0;
		if (speed == 0)
			target = 0;
		else if (speed < medium)
			target = Mth.lerp(speed / medium, 0, .45f);
		else if (speed < fast)
			target = Mth.lerp((speed - medium) / (fast - medium), .45f, .75f);
		else
			target = Mth.lerp((speed - fast) / (max - fast), .75f, 1.125f);
		return target;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		Lang.translate("gui.speedometer.title")
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip);
		SpeedLevel.getFormattedSpeedText(speed, isOverStressed())
			.forGoggles(tooltip);
		return true;
	}
}
