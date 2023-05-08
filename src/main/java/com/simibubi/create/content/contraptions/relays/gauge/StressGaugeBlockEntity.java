package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.List;

import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StressGaugeBlockEntity extends GaugeBlockEntity {

	static BlockPos lastSent;

	public StressGaugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		registerAwardables(behaviours, AllAdvancements.STRESSOMETER, AllAdvancements.STRESSOMETER_MAXED);
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
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!StressImpact.isEnabled())
			return false;

		super.addToGoggleTooltip(tooltip, isPlayerSneaking);

		double capacity = getNetworkCapacity();
		double stressFraction = getNetworkStress() / (capacity == 0 ? 1 : capacity);

		Lang.translate("gui.stressometer.title")
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip);

		if (getTheoreticalSpeed() == 0)
			Lang.text(TooltipHelper.makeProgressBar(3, 0))
				.translate("gui.stressometer.no_rotation")
				.style(ChatFormatting.DARK_GRAY)
				.forGoggles(tooltip);
		else {
			StressImpact.getFormattedStressText(stressFraction)
				.forGoggles(tooltip);
			Lang.translate("gui.stressometer.capacity")
				.style(ChatFormatting.GRAY)
				.forGoggles(tooltip);

			double remainingCapacity = capacity - getNetworkStress();

			LangBuilder su = Lang.translate("generic.unit.stress");
			LangBuilder stressTip = Lang.number(remainingCapacity)
				.add(su)
				.style(StressImpact.of(stressFraction)
					.getRelativeColor());

			if (remainingCapacity != capacity)
				stressTip.text(ChatFormatting.GRAY, " / ")
					.add(Lang.number(capacity)
						.add(su)
						.style(ChatFormatting.DARK_GRAY));

			stressTip.forGoggles(tooltip, 1);
		}

		if (!worldPosition.equals(lastSent))
			AllPackets.getChannel().sendToServer(new GaugeObservedPacket(lastSent = worldPosition));

		return true;
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (clientPacket && worldPosition != null && worldPosition.equals(lastSent))
			lastSent = null;
	}

	public float getNetworkStress() {
		return stress;
	}

	public float getNetworkCapacity() {
		return capacity;
	}

	public void onObserved() {
		award(AllAdvancements.STRESSOMETER);
		if (Mth.equal(dialTarget, 1))
			award(AllAdvancements.STRESSOMETER_MAXED);
	}

}
