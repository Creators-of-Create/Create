package com.simibubi.create.content.decoration.steamWhistle;

import java.lang.ref.WeakReference;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.simibubi.create.content.decoration.steamWhistle.WhistleExtenderBlock.WhistleExtenderShape;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.animation.LerpedFloat.Chaser;
import net.createmod.catnip.api.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.api.distmarker.Dist;

public class WhistleBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

	public WeakReference<FluidTankBlockEntity> source;
	public LerpedFloat animation;
	protected int pitch;

	public WhistleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		source = new WeakReference<>(null);
		animation = LerpedFloat.linear();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		registerAwardables(behaviours, AllAdvancements.STEAM_WHISTLE);
	}

	public void updatePitch() {
		BlockPos currentPos = worldPosition.above();
		int newPitch;
		for (newPitch = 0; newPitch <= 24; newPitch += 2) {
			BlockState blockState = level.getBlockState(currentPos);
			if (!AllBlocks.STEAM_WHISTLE_EXTENSION.has(blockState))
				break;
			if (blockState.getValue(WhistleExtenderBlock.SHAPE) == WhistleExtenderShape.SINGLE) {
				newPitch++;
				break;
			}
			currentPos = currentPos.above();
		}
		if (pitch == newPitch)
			return;
		pitch = newPitch;

		notifyUpdate();

		FluidTankBlockEntity tank = getTank();
		if (tank != null && tank.boiler != null)
			tank.boiler.checkPipeOrganAdvancement(tank);
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide()) {
			if (isPowered())
				award(AllAdvancements.STEAM_WHISTLE);
			return;
		}

		FluidTankBlockEntity tank = getTank();
		boolean powered = isPowered()
			&& (tank != null && tank.boiler.isActive() && (tank.boiler.passiveHeat || tank.boiler.activeHeat > 0)
			|| isVirtual());
		animation.chase(powered ? 1 : 0, powered ? .5f : .4f, powered ? Chaser.EXP : Chaser.LINEAR);
		animation.tickChaser();
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> WhistleClient.tickAudio(this, getOctave(), powered));
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		tag.putInt("Pitch", pitch);
		super.write(tag, registries, clientPacket);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		pitch = tag.getIntOr("Pitch", 0);
		super.read(tag, registries, clientPacket);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		String[] pitches = CreateLang.translateDirect("generic.notes")
			.getString()
			.split(";");
		CreateLang.translate("generic.pitch", pitches[pitch % pitches.length]).forGoggles(tooltip);
		return true;
	}

	protected boolean isPowered() {
		return getBlockState().getOptionalValue(WhistleBlock.POWERED)
			.orElse(false);
	}

	protected WhistleSize getOctave() {
		return getBlockState().getOptionalValue(WhistleBlock.SIZE)
			.orElse(WhistleSize.MEDIUM);
	}

	public int getPitchId() {
		return pitch + 100 * getBlockState().getOptionalValue(WhistleBlock.SIZE)
			.orElse(WhistleSize.MEDIUM)
			.ordinal();
	}

	public FluidTankBlockEntity getTank() {
		FluidTankBlockEntity tank = source.get();
		if (tank == null || tank.isRemoved()) {
			if (tank != null)
				source = new WeakReference<>(null);
			Direction facing = WhistleBlock.getAttachedDirection(getBlockState());
			BlockEntity be = level.getBlockEntity(worldPosition.relative(facing));
			if (be instanceof FluidTankBlockEntity tankBe)
				source = new WeakReference<>(tank = tankBe);
		}
		if (tank == null)
			return null;
		return tank.getControllerBE();
	}

}
