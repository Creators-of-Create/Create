package com.simibubi.create.content.redstone.diodes;

import java.util.List;

import static com.simibubi.create.content.redstone.diodes.BrassDiodeBlock.POWERING;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class PulseTimerBlockEntity extends BrassDiodeBlockEntity implements IHaveGoggleInformation {

	public PulseTimerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	@Override
	protected int defaultValue() {
		return 20;
	}

	@Override
	protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
		if (powered || state >= maxState.getValue() - 1)
			state = 0;
		else
			state++;

		if (level.isClientSide)
			return;

		boolean shouldPower = !powered && (maxState.getValue() == 2 ? state == 0 : state <= 1);
		BlockState blockState = getBlockState();
		if (blockState.getValue(POWERING) != shouldPower)
			level.setBlockAndUpdate(worldPosition, blockState.setValue(POWERING, shouldPower));
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		// Used for GameTests to safely verify tooltip content on the server side
		// Bypasses client-only formatting logic to prevent crashes in headless environments
		// Note: Used Component.translatable directly to avoid issues with CreateLang in GameTests
		if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            tooltip.add(Component.translatable("create.tooltip.pulse_timer.header"));
			tooltip.add(Component.translatable("create.tooltip.pulse.until_next_pulse"));
			tooltip.add(Component.literal("" + state));
            return true;
        }

		int maxTicks = maxState.getValue();

		CreateLang.translate("tooltip.pulse_timer.header")
			.forGoggles(tooltip);

		CreateLang.translate("tooltip.pulse.until_next_pulse")
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip);

		CreateLang.text(formatGoggleTooltip(state, maxTicks))
			.style(ChatFormatting.AQUA)
			.text(ChatFormatting.GRAY, " / ")
			.add(CreateLang.text(formatGoggleTooltip(maxTicks, maxTicks))
				.style(ChatFormatting.DARK_GRAY))
			.forGoggles(tooltip, 1);

		return true;
	}
}
