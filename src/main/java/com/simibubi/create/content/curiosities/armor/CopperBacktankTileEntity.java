package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.foundation.tileEntity.ComparatorUtil;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;

public class CopperBacktankTileEntity extends KineticTileEntity implements Nameable {

	public int airLevel;
	public int airLevelTimer;
	private Component customName;

	private int capacityEnchantLevel;
	private ListTag enchantmentTag;

	public CopperBacktankTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		enchantmentTag = new ListTag();
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

		int max = BackTankUtil.maxAir(capacityEnchantLevel);
		if (level.isClientSide) {
			Vec3 centerOf = VecHelper.getCenterOf(worldPosition);
			Vec3 v = VecHelper.offsetRandomly(centerOf, Create.RANDOM, .65f);
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
		if (getComparatorOutput() != prevComparatorLevel && !level.isClientSide)
			level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
		if (airLevel == max)
			sendData();
		airLevelTimer = Mth.clamp((int) (128f - abs / 5f) - 108, 0, 20);
	}

	public int getComparatorOutput() {
		int max = BackTankUtil.maxAir(capacityEnchantLevel);
		return ComparatorUtil.fractionToRedstoneLevel(airLevel / (float) max);
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("Air", airLevel);
		compound.putInt("Timer", airLevelTimer);
		compound.putInt("CapacityEnchantment", capacityEnchantLevel);
		if (this.customName != null)
			compound.putString("CustomName", Component.Serializer.toJson(this.customName));
		compound.put("Enchantments", enchantmentTag);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		int prev = airLevel;
		capacityEnchantLevel = compound.getInt("CapacityEnchantment");
		airLevel = compound.getInt("Air");
		airLevelTimer = compound.getInt("Timer");
		enchantmentTag = compound.getList("Enchantments", Tag.TAG_COMPOUND);
		if (compound.contains("CustomName", 8))
			this.customName = Component.Serializer.fromJson(compound.getString("CustomName"));
		if (prev != 0 && prev != airLevel && airLevel == BackTankUtil.maxAir(capacityEnchantLevel) && clientPacket)
			playFilledEffect();
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
		return this.customName != null ? this.customName
			: new TranslatableComponent(AllItems.COPPER_BACKTANK.get()
				.getDescriptionId());
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

	public Component getCustomName() {
		return customName;
	}

	public ListTag getEnchantmentTag() {
		return enchantmentTag;
	}

	public void setEnchantmentTag(ListTag enchantmentTag) {
		this.enchantmentTag = enchantmentTag;
	}

	public void setCapacityEnchantLevel(int capacityEnchantLevel) {
		this.capacityEnchantLevel = capacityEnchantLevel;
	}

}
