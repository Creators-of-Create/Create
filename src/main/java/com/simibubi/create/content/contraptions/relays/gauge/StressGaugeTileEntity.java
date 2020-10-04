package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.List;

import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class StressGaugeTileEntity extends GaugeTileEntity {

	public StressGaugeTileEntity(TileEntityType<? extends StressGaugeTileEntity> type) {
		super(type);
	}

	@Override
	public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
		super.updateFromNetwork(maxStress, currentStress, networkSize);

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

		updateFromNetwork(capacity, stress, getOrCreateNetwork().getSize());
	}

	@Override
	public boolean addToGoggleTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking) {
		if (!StressImpact.isEnabled())
			return false;

		super.addToGoggleTooltip(tooltip, isPlayerSneaking);

		double capacity = getNetworkCapacity();
		double stressFraction = getNetworkStress() / (capacity == 0 ? 1 : capacity);

		tooltip.add(componentSpacing.copy().append(Lang.translate("gui.stressometer.title").formatted(TextFormatting.GRAY)));

		if (getTheoreticalSpeed() == 0)
			tooltip.add(ITextComponent.of(TextFormatting.DARK_GRAY + ItemDescription.makeProgressBar(3, -1)
					+ Lang.translate("gui.stressometer.no_rotation")));
		else {
			tooltip.add(componentSpacing.copy().append(StressImpact.getFormattedStressText(stressFraction)));

			tooltip.add(componentSpacing.copy().append(Lang.translate("gui.stressometer.capacity").formatted(TextFormatting.GRAY)));

			double remainingCapacity = capacity - getNetworkStress();
			double remainingCapacityAtBase = remainingCapacity / Math.abs(getTheoreticalSpeed());

			tooltip.add(componentSpacing.copy().append(new StringTextComponent(IHaveGoggleInformation.format(remainingCapacityAtBase))
				.append(Lang.translate("generic.unit.stress")).append(" ").formatted(StressImpact.of(stressFraction).getRelativeColor()))
				.append(Lang.translate("gui.goggles.base_value").formatted(TextFormatting.DARK_GRAY)));

			tooltip.add(componentSpacing.copy().append(new StringTextComponent(IHaveGoggleInformation.format(remainingCapacity))
				.append(Lang.translate("generic.unit.stress")).append(" ").formatted(StressImpact.of(stressFraction).getRelativeColor()))
				.append(Lang.translate("gui.goggles.at_current_speed").formatted(TextFormatting.DARK_GRAY)));
		}

		return true;
	}

	public float getNetworkStress() {
		return stress;
	}

	public float getNetworkCapacity() {
		return capacity;
	}

}
