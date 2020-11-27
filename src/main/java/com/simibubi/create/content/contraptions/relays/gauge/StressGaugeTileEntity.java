package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.List;

import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.tileentity.TileEntityType;
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
	public boolean addToGoggleTooltip(List<String> tooltip, boolean isPlayerSneaking) {
		if (!StressImpact.isEnabled())
			return false;

		super.addToGoggleTooltip(tooltip, isPlayerSneaking);

		double capacity = getNetworkCapacity();
		double stressFraction = getNetworkStress() / (capacity == 0 ? 1 : capacity);

		tooltip.add(spacing + TextFormatting.GRAY + Lang.translate("gui.stressometer.title"));

		if (getTheoreticalSpeed() == 0)
			tooltip.add(spacing + TextFormatting.DARK_GRAY + ItemDescription.makeProgressBar(3, -1)
				+ Lang.translate("gui.stressometer.no_rotation"));
		else {
			tooltip.add(spacing + StressImpact.getFormattedStressText(stressFraction));

			tooltip.add(spacing + TextFormatting.GRAY + Lang.translate("gui.stressometer.capacity"));

			double remainingCapacity = capacity - getNetworkStress();

			String su = Lang.translate("generic.unit.stress");

			if (remainingCapacity != capacity) {
				String capacityString = spacing + StressImpact.of(stressFraction)
					.getRelativeColor() + "%s" + su + TextFormatting.GRAY + " / " + TextFormatting.DARK_GRAY + "%s"
					+ su;
				tooltip.add(" " + String.format(capacityString, IHaveGoggleInformation.format(remainingCapacity),
					IHaveGoggleInformation.format(capacity)));
			} else {
				String capacityString = spacing + StressImpact.of(stressFraction)
					.getRelativeColor() + "%s" + su;
				tooltip.add(" " + String.format(capacityString, IHaveGoggleInformation.format(remainingCapacity)));
			}
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
