package com.simibubi.create.content.contraptions.base;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class GeneratingKineticTileEntity extends KineticTileEntity {

	public boolean reActivateSource;

	public GeneratingKineticTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	@Override
	public void tick() {
		super.tick();
		if (reActivateSource) {
			updateGeneratedRotation();
			reActivateSource = false;
		}
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);

//		float stressBase = calculateAddedStressCapacity();
//		if (stressBase != 0 && IRotate.StressImpact.isEnabled()) {
//			tooltip.add(componentSpacing.plainCopy().append(Lang.translate("gui.goggles.generator_stats")));
//			tooltip.add(componentSpacing.plainCopy().append(Lang.translate("tooltip.capacityProvided").withStyle(ChatFormatting.GRAY)));
//
//			float speed = getTheoreticalSpeed();
//			if (speed != getGeneratedSpeed() && speed != 0)
//				stressBase *= getGeneratedSpeed() / speed;
//
//			speed = Math.abs(speed);
//			float stressTotal = stressBase * speed;
//
//			tooltip.add(
//					componentSpacing.plainCopy()
//					.append(new TextComponent(" " + IHaveGoggleInformation.format(stressTotal))
//							.append(Lang.translate("generic.unit.stress"))
//							.withStyle(ChatFormatting.AQUA))
//					.append(" ")
//					.append(Lang.translate("gui.goggles.at_current_speed").withStyle(ChatFormatting.DARK_GRAY)));
//
//			added = true;
//		}

		return added;
	}

	public void updateGeneratedRotation() {
//		float speed = getGeneratedSpeed();
//		float prevSpeed = this.speed;
//
//		if (level.isClientSide)
//			return;
//
//		if (prevSpeed != speed) {
//			if (!hasSource()) {
//				SpeedLevel levelBefore = SpeedLevel.of(this.speed);
//				SpeedLevel levelafter = SpeedLevel.of(speed);
//				if (levelBefore != levelafter)
//					effects.queueRotationIndicators();
//			}
//
//			applyNewSpeed(prevSpeed, speed);
//		}
//
//		if (hasNetwork() && speed != 0) {
//			KineticNetwork network = getOrCreateNetwork();
//			notifyStressCapacityChange(calculateAddedStressCapacity());
//			getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
//			network.updateStress();
//		}
//
//		onSpeedChanged(prevSpeed);
//		sendData();
	}

	public Long createNetworkId() {
		return worldPosition.asLong();
	}
}
