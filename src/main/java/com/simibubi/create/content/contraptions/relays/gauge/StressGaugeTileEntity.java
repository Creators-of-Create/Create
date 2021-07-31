package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.List;

import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.IFormattableTextComponent;
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
		else if (isOverStressed())
			dialTarget = 1.125f;
		else if (maxStress == 0)
			dialTarget = 0;
		else
			dialTarget = currentStress / maxStress;

		if (dialTarget > 0) {
			if (dialTarget < .5f)
				color = Color.mixColors(0x00FF00, 0xFFFF00, dialTarget * 2);
			else if (dialTarget < 1)
				color = Color.mixColors(0xFFFF00, 0xFF0000, (dialTarget) * 2 - 1);
			else
				color = 0xFF0000;
		}

		sendData();
		setChanged();
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		if (getSpeed() == 0) {
			dialTarget = 0;
			setChanged();
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

		tooltip.add(componentSpacing.plainCopy().append(Lang.translate("gui.stressometer.title").withStyle(TextFormatting.GRAY)));

		if (getTheoreticalSpeed() == 0)
			tooltip.add(new StringTextComponent(spacing + ItemDescription.makeProgressBar(3, -1)).append(Lang.translate("gui.stressometer.no_rotation")).withStyle(TextFormatting.DARK_GRAY));
		//	tooltip.add(new StringTextComponent(TextFormatting.DARK_GRAY + ItemDescription.makeProgressBar(3, -1)
		//			+ Lang.translate("gui.stressometer.no_rotation")));
		else {
			tooltip.add(componentSpacing.plainCopy().append(StressImpact.getFormattedStressText(stressFraction)));

			tooltip.add(componentSpacing.plainCopy().append(Lang.translate("gui.stressometer.capacity").withStyle(TextFormatting.GRAY)));

			double remainingCapacity = capacity - getNetworkStress();

			ITextComponent su = Lang.translate("generic.unit.stress");
			IFormattableTextComponent stressTooltip = componentSpacing.plainCopy()
					.append(new StringTextComponent(" " + IHaveGoggleInformation.format(remainingCapacity))
							.append(su.plainCopy())
							.withStyle(StressImpact.of(stressFraction).getRelativeColor()));
			if (remainingCapacity != capacity) {
				stressTooltip
						.append(new StringTextComponent(" / ").withStyle(TextFormatting.GRAY))
						.append(new StringTextComponent(IHaveGoggleInformation.format(capacity))
								.append(su.plainCopy())
								.withStyle(TextFormatting.DARK_GRAY));
			}
			tooltip.add(stressTooltip);
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
