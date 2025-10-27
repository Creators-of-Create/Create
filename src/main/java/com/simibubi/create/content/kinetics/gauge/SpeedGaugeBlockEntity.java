package com.simibubi.create.content.kinetics.gauge;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class SpeedGaugeBlockEntity extends GaugeBlockEntity {

	public AbstractComputerBehaviour computerBehaviour;

	public SpeedGaugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		if (Mods.COMPUTERCRAFT.isLoaded()) {
			event.registerBlockEntity(
					PeripheralCapability.get(),
					AllBlockEntityTypes.SPEEDOMETER.get(),
					(be, context) -> be.computerBehaviour.getPeripheralCapability()
			);
		}
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		if (computerBehaviour.hasAttachedComputer())
			computerBehaviour.prepareComputerEvent(makeComputerKineticsChangeEvent());
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
		CreateLang.translate("gui.speedometer.title")
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip);
		SpeedLevel.getFormattedSpeedText(speed, isOverStressed())
			.forGoggles(tooltip);
		return true;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		computerBehaviour.removePeripheral();
	}
}
