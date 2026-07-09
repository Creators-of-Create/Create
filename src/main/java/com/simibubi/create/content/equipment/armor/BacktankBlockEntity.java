package com.simibubi.create.content.equipment.armor;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.ComparatorUtil;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.particle.AirParticleData;
import com.simibubi.create.foundation.utility.LegacyComponentSerializationBridge;

import net.createmod.catnip.api.data.codec.CatnipCodecUtils;
import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;

public class BacktankBlockEntity extends KineticBlockEntity implements Nameable {

	public int airLevel;
	public int airLevelTimer;
	private Component defaultName;
	private Component customName;

	private int capacityEnchantLevel;

	private DataComponentPatch componentPatch;

	public BacktankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		defaultName = getDefaultName(state);
		componentPatch = DataComponentPatch.EMPTY;
	}

	public static Component getDefaultName(BlockState state) {
		if (AllBlocks.NETHERITE_BACKTANK.has(state)) {
			return AllItems.NETHERITE_BACKTANK.asStack()
				.getHoverName();
		}

		return AllItems.COPPER_BACKTANK.asStack()
			.getHoverName();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		registerAwardables(behaviours, AllAdvancements.BACKTANK);
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
		if (getSpeed() != 0)
			award(AllAdvancements.BACKTANK);
	}

	@Override
	public void tick() {
		super.tick();
		if (getSpeed() == 0)
			return;

		BlockState state = getBlockState();
		BooleanProperty waterProperty = BlockStateProperties.WATERLOGGED;
		if (state.hasProperty(waterProperty) && state.getValue(waterProperty))
			return;

		if (airLevelTimer > 0) {
			airLevelTimer--;
			return;
		}

		int max = BacktankUtil.maxAir(capacityEnchantLevel);
		if (level.isClientSide()) {
			Vec3 centerOf = VecHelper.getCenterOf(worldPosition);
			Vec3 v = VecHelper.offsetRandomly(centerOf, level.getRandom(), .65f);
			Vec3 m = centerOf.subtract(v);
			if (airLevel != max)
				level.addParticle(new AirParticleData(1, .05f), v.x, v.y, v.z, m.x, m.y, m.z);
			return;
		}

		if (airLevel == max)
			return;

		int prevComparatorLevel = getComparatorOutput();
		float abs = Math.abs(getSpeed());
		int increment = Mth.clamp(((int) abs - 100) / 20, 1, 5);
		airLevel = Math.min(max, airLevel + increment);
		if (getComparatorOutput() != prevComparatorLevel && !level.isClientSide())
			level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
		if (airLevel == max)
			sendData();
		airLevelTimer = Mth.clamp((int) (128f - abs / 5f) - 108, 0, 20);
	}

	public int getComparatorOutput() {
		int max = BacktankUtil.maxAir(capacityEnchantLevel);
		return ComparatorUtil.fractionToRedstoneLevel(airLevel / (float) max);
	}

	@Override
	protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(compound, registries, clientPacket);
		compound.putInt("Air", airLevel);
		compound.putInt("Timer", airLevelTimer);
		compound.putInt("CapacityEnchantment", capacityEnchantLevel);

		if (this.customName != null)
			compound.putString("CustomName", LegacyComponentSerializationBridge.toJson(this.customName, registries));

		compound.put("Components", CatnipCodecUtils.encode(DataComponentPatch.CODEC, registries, componentPatch)
			.orElse(new CompoundTag()));
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(compound, registries, clientPacket);
		int prev = airLevel;
		airLevel = compound.getIntOr("Air", 0);
		airLevelTimer = compound.getIntOr("Timer", 0);
		capacityEnchantLevel = compound.getIntOr("CapacityEnchantment", 0);

		if (compound.contains("CustomName"))
			this.customName =
				LegacyComponentSerializationBridge.fromJson(compound.getStringOr("CustomName", ""), registries);

		componentPatch = CatnipCodecUtils.decode(DataComponentPatch.CODEC, registries,
			compound.getCompoundOrEmpty("Components")).orElse(DataComponentPatch.EMPTY);
		if (prev != 0 && prev != airLevel && airLevel == BacktankUtil.maxAir(capacityEnchantLevel) && clientPacket)
			playFilledEffect();
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter componentInput) {
		setAirLevel(componentInput.getOrDefault(AllDataComponents.BACKTANK_AIR, 0));
	}

	@Override
	protected void collectImplicitComponents(Builder components) {
		components.set(AllDataComponents.BACKTANK_AIR, airLevel);
	}

	protected void playFilledEffect() {
		AllSoundEvents.CONFIRM.playAt(level, worldPosition, 0.4f, 1, true);
		Vec3 baseMotion = new Vec3(.25, 0.1, 0);
		Vec3 baseVec = VecHelper.getCenterOf(worldPosition);
		for (int i = 0; i < 360; i += 10) {
			Vec3 m = VecHelper.rotate(baseMotion, i, Axis.Y);
			Vec3 v = baseVec.add(m.normalize()
				.scale(.25f));

			level.addParticle(ParticleTypes.SPIT, v.x, v.y, v.z, m.x, m.y, m.z);
		}
	}

	@Override
	public Component getName() {
		return this.customName != null ? this.customName : defaultName;
	}

	public int getAirLevel() {
		return airLevel;
	}

	public void setAirLevel(int airLevel) {
		this.airLevel = airLevel;
		sendData();
	}

	public void setCustomName(Component customName) {
		this.customName = customName;
	}

	public void setCapacityEnchantLevel(int capacityEnchantLevel) {
		this.capacityEnchantLevel = capacityEnchantLevel;
	}

	public void setComponentPatch(DataComponentPatch componentPatch) {
		this.componentPatch = componentPatch;
	}

	public DataComponentPatch getComponentPatch() {
		return componentPatch;
	}

}
