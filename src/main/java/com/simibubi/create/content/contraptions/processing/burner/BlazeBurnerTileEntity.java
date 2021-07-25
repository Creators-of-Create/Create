package com.simibubi.create.content.contraptions.processing.burner;

import java.util.List;
import java.util.Random;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.particle.CubeParticleData;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeHooks;

public class BlazeBurnerTileEntity extends SmartTileEntity {

	public static final int MAX_HEAT_CAPACITY = 10000;

	private static final Vector3d EMPTY_COLOR = new Vector3d(0, 0, 0);
	private final static Vector3d[][] PARTICLE_COLORS;
	private final static Vector3d[] CREATIVE_PARTICLE_COLORS;
	static {
		int[][] colors = {
			{ },
			{ 0x3B141A, 0x47141A, 0x7A3B24, 0x854D26 },
			{ 0x2A0103, 0x741B0A, 0xC38246, 0xCCBD78 },
			{ 0x630B03, 0x8B3503, 0xBC8200, 0xCCC849 },
			{ 0x1C6378, 0x4798B5, 0x4DA6C0, 0xBAC8CE }
		};

		int[] creativeColors = { 0x54295D, 0x6E3C76, 0xA5479F, 0x85157C };

		PARTICLE_COLORS = new Vector3d[colors.length][];
		for (int i = 0; i < colors.length; i++) {
			PARTICLE_COLORS[i] = ColorHelper.toVectors(colors[i]);
		}

		CREATIVE_PARTICLE_COLORS = ColorHelper.toVectors(creativeColors);
	}

	protected FuelType activeFuel;
	protected int remainingBurnTime;
	protected LerpedFloat headAngle;
	protected boolean isCreative;

	public BlazeBurnerTileEntity(TileEntityType<? extends BlazeBurnerTileEntity> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		activeFuel = FuelType.NONE;
		remainingBurnTime = 0;
		headAngle = LerpedFloat.angular();
		isCreative = false;
	}

	public FuelType getActiveFuel() {
		return activeFuel;
	}

	public int getRemainingBurnTime() {
		return remainingBurnTime;
	}

	public boolean isCreative() {
		return isCreative;
	}

	@Override
	public void tick() {
		super.tick();

		if (level.isClientSide) {
			tickRotation();
			spawnParticles(getHeatLevelFromBlock(), 1);
			return;
		}

		if (isCreative)
			return;

		if (remainingBurnTime > 0)
			remainingBurnTime--;

		if (activeFuel == FuelType.NORMAL)
			updateBlockState();
		if (remainingBurnTime > 0)
			return;

		if (activeFuel == FuelType.SPECIAL) {
			activeFuel = FuelType.NORMAL;
			remainingBurnTime = MAX_HEAT_CAPACITY / 2;
		} else
			activeFuel = FuelType.NONE;

		updateBlockState();
		notifyUpdate();
	}

	private void tickRotation() {
		float target = 0;
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player != null) {
			double x;
			double z;
			if (isVirtual()) {
				x = -4;
				z = -10;
			} else {
				x = player.getX();
				z = player.getZ();
			}
			double dx = x - (getBlockPos().getX() + 0.5);
			double dz = z - (getBlockPos().getZ() + 0.5);
			target = AngleHelper.deg(-MathHelper.atan2(dz, dx)) - 90;
		}
		target = headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), target);
		headAngle.chase(target, .25f, Chaser.exp(5));
		headAngle.tickChaser();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		if (!isCreative) {
			compound.putInt("fuelLevel", activeFuel.ordinal());
			compound.putInt("burnTimeRemaining", remainingBurnTime);
		} else {
			compound.putBoolean("isCreative", true);
		}
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		activeFuel = FuelType.values()[compound.getInt("fuelLevel")];
		remainingBurnTime = compound.getInt("burnTimeRemaining");
		isCreative = compound.getBoolean("isCreative");
		super.fromTag(state, compound, clientPacket);
	}

	public BlazeBurnerBlock.HeatLevel getHeatLevelFromBlock() {
		return BlazeBurnerBlock.getHeatLevelOf(getBlockState());
	}

	public void updateBlockState() {
		setBlockHeat(getHeatLevelFromFuelType(activeFuel));
	}

	protected void setBlockHeat(HeatLevel heat) {
		HeatLevel inBlockState = getHeatLevelFromBlock();
		if (inBlockState == heat)
			return;
		level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlazeBurnerBlock.HEAT_LEVEL, heat));
		notifyUpdate();
	}

	/**
	 * @return true if the heater updated its burn time and an item should be
	 *         consumed
	 */
	protected boolean tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate) {
		if (isCreative)
			return false;

		FuelType newFuel = FuelType.NONE;
		int newBurnTime;

		if (AllItems.BLAZE_CAKE.isIn(itemStack)) {
			newBurnTime = 1000;
			newFuel = FuelType.SPECIAL;
		} else {
			newBurnTime = ForgeHooks.getBurnTime(itemStack);
			if (newBurnTime > 0)
				newFuel = FuelType.NORMAL;
		}

		if (newFuel == FuelType.NONE)
			return false;
		if (newFuel.ordinal() < activeFuel.ordinal())
			return false;
		if (activeFuel == FuelType.SPECIAL && remainingBurnTime > 20)
			return false;

		if (newFuel == activeFuel) {
			if (remainingBurnTime + newBurnTime > MAX_HEAT_CAPACITY && !forceOverflow)
				return false;
			newBurnTime = MathHelper.clamp(remainingBurnTime + newBurnTime, 0, MAX_HEAT_CAPACITY);
		}

		if (simulate)
			return true;

		activeFuel = newFuel;
		remainingBurnTime = newBurnTime;

		if (level.isClientSide) {
			HeatLevel level = getHeatLevelFromFuelType(activeFuel);
			for (int i = 0; i < 20; i++)
				spawnParticles(level, 1 + (.25 * (i / 4)));
		} else {
			playSound();
			updateBlockState();
		}

		return true;
	}

	protected void applyCreativeFuel() {
		boolean wasCreative = isCreative;

		activeFuel = FuelType.NONE;
		remainingBurnTime = 0;
		isCreative = true;

		if (level.isClientSide) {
			for (int i = 0; i < 30; i++) {
				double burstMult = 1 + (.25 * (i / 4));
				spawnParticle(CREATIVE_PARTICLE_COLORS, 0.04F, 35, false, 0.03 * burstMult, 0.15 * burstMult);
			}
		} else {
			playSound();
			if (wasCreative)
				setBlockHeat(getHeatLevelFromBlock().nextActiveLevel());
		}
	}

	public boolean isCreativeFuel(ItemStack stack) {
		return AllItems.CREATIVE_BLAZE_CAKE.isIn(stack);
	}

	protected void playSound() {
		level.playSound(null, worldPosition, SoundEvents.BLAZE_SHOOT, SoundCategory.BLOCKS,
			.125f + level.random.nextFloat() * .125f, .75f - level.random.nextFloat() * .25f);
	}

	protected HeatLevel getHeatLevelFromFuelType(FuelType fuel) {
		HeatLevel level = HeatLevel.SMOULDERING;
		switch (activeFuel) {
		case SPECIAL:
			level = HeatLevel.SEETHING;
			break;
		case NORMAL:
			boolean lowPercent = (double) remainingBurnTime / MAX_HEAT_CAPACITY < 0.1;
			level = lowPercent ? HeatLevel.FADING : HeatLevel.KINDLED;
			break;
		default:
		case NONE:
			break;
		}
		return level;
	}

	protected void spawnParticles(HeatLevel heatLevel, double burstMult) {
		if (level == null)
			return;
		if (heatLevel == BlazeBurnerBlock.HeatLevel.NONE)
			return;

		Random r = level.getRandom();
		switch (heatLevel) {
		case SMOULDERING:
			if (r.nextDouble() > 0.25)
				return;
			spawnParticle(heatLevel, 0.03F, 15, false, 0.015 * burstMult, 0.1 * burstMult);
			break;
		case FADING:
			if (r.nextDouble() > 0.5)
				return;
			spawnParticle(heatLevel, 0.035F, 18, false, 0.03 * burstMult, 0.15 * burstMult);
			break;
		case KINDLED:
			spawnParticle(heatLevel, 0.04F, 35, true, 0.05 * burstMult, 0.2 * burstMult);
			break;
		case SEETHING:
			for (int i = 0; i < 2; i++) {
				if (r.nextDouble() > 0.6)
					return;
				spawnParticle(heatLevel, 0.045F, 35, true, 0.06 * burstMult, 0.22 * burstMult);
			}
			break;
		default:
			break;
		}
	}

	protected void spawnParticle(Vector3d color, float scale, int avgAge, boolean hot, double speed, double spread) {
		Random random = level.getRandom();
		level.addAlwaysVisibleParticle(
			new CubeParticleData((float) color.x, (float) color.y, (float) color.z, scale, avgAge, hot),
			(double) worldPosition.getX() + 0.5D + (random.nextDouble() * 2.0 - 1D) * spread,
			(double) worldPosition.getY() + 0.6D + (random.nextDouble() / 4.0),
			(double) worldPosition.getZ() + 0.5D + (random.nextDouble() * 2.0 - 1D) * spread, 0.0D, speed, 0.0D);
	}

	protected void spawnParticle(Vector3d[] colors, float scale, int avgAge, boolean hot, double speed, double spread) {
		Vector3d color;
		if (colors.length == 0) {
			color = EMPTY_COLOR;
		} else {
			color = colors[(int) (Math.random() * colors.length)];
		}
		spawnParticle(color, scale, avgAge, hot, speed, spread);
	}

	protected void spawnParticle(HeatLevel heatLevel, float scale, int avgAge, boolean hot, double speed, double spread) {
		spawnParticle(PARTICLE_COLORS[heatLevel.ordinal()], scale, avgAge, hot, speed, spread);
	}

	public enum FuelType {
		NONE, NORMAL, SPECIAL
	}

}
