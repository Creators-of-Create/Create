package com.simibubi.create.content.redstone.diodes;

import java.util.List;

import static com.simibubi.create.content.redstone.diodes.BrassDiodeBlock.POWERING;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class PulseExtenderBlockEntity extends BrassDiodeBlockEntity implements IHaveGoggleInformation {

	public PulseExtenderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
		if (atMin && !powered)
			return;
		if (atMin || powered) {
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERING, true));
			state = maxState.getValue();
			return;
		}
		
		if (state == 1) {
			if (powering && !level.isClientSide)
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERING, false));
			if (!powered)
				state = 0;
			return;
		}
		
		if (!powered)
			state--;
	}
	
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		// Used for GameTests to safely verify tooltip content on the server side
		// Bypasses client-only formatting logic to prevent crashes in headless environments
		// Note: Used Component.translatable directly to avoid issues with CreateLang in GameTests
		if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            tooltip.add(Component.translatable("create.tooltip.pulse_extender.header"));
			tooltip.add(Component.translatable("create.tooltip.pulse_extender.remaining"));
			tooltip.add(Component.literal("" + state));
            return true;
        }

		int maxTicks = maxState.getValue();

		CreateLang.translate("tooltip.pulse_extender.header")
			.forGoggles(tooltip);

		CreateLang.translate("tooltip.pulse_extender.remaining")
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
