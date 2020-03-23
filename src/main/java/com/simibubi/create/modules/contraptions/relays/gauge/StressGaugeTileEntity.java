package com.simibubi.create.modules.contraptions.relays.gauge;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.modules.contraptions.goggle.IHaveGoggleInformation;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

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

	@Override
	public boolean addToGoggleTooltip(List<String> tooltip, boolean isPlayerSneaking) {
		if (!StressImpact.isEnabled())
			return false;

		super.addToGoggleTooltip(tooltip, isPlayerSneaking);

		double capacity = getNetworkCapacity();
		double stressFraction = getNetworkStress() / (capacity == 0 ? 1 : capacity);

		tooltip.add(spacing + TextFormatting.GRAY + Lang.translate("gui.stress_gauge.title"));

		if (getTheoreticalSpeed() == 0)
			tooltip.add(TextFormatting.DARK_GRAY + ItemDescription.makeProgressBar(3, -1) + Lang.translate("gui.stress_gauge.no_rotation"));
		else {
			tooltip.add(spacing + StressImpact.getFormattedStressText(stressFraction));

			tooltip.add(spacing + TextFormatting.GRAY + Lang.translate("gui.stress_gauge.capacity"));

			double remainingCapacity = capacity - getNetworkStress();
			double remainingCapacityAtBase = remainingCapacity / Math.abs(getTheoreticalSpeed());

			String capacityString = spacing + StressImpact.of(stressFraction).getRelativeColor() + "%s" + Lang.translate("generic.unit.stress") + " " + TextFormatting.DARK_GRAY + "%s";

			tooltip.add(String.format(capacityString, IHaveGoggleInformation.format(remainingCapacityAtBase), Lang.translate("gui.goggles.base_value")));
			tooltip.add(String.format(capacityString, IHaveGoggleInformation.format(remainingCapacity), Lang.translate("gui.goggles.at_current_speed")));

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
