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
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;

public class BlazeBurnerTileEntity extends SmartTileEntity {

	private final static int[][] heatParticleColors =
		{ { 0x3B141A, 0x47141A, 0x7A3B24, 0x854D26 }, { 0x2A0103, 0x741B0A, 0xC38246, 0xCCBD78 },
			{ 0x630B03, 0x8B3503, 0xBC8200, 0xCCC849 }, { 0x1C6378, 0x4798B5, 0x4DA6C0, 0xBAC8CE } };

	public static final int maxHeatCapacity = 10000;

	public static enum FuelType {
		NONE, NORMAL, SPECIAL
	}

	int remainingBurnTime;
	FuelType activeFuel;
	LerpedFloat headAngle;

	public BlazeBurnerTileEntity(TileEntityType<? extends BlazeBurnerTileEntity> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		activeFuel = FuelType.NONE;
		remainingBurnTime = 0;
		headAngle = LerpedFloat.angular();
	}

	@Override
	public void tick() {
		super.tick();
		if (world.isRemote) {
			tickRotation();
			spawnParticles(getHeatLevelFromBlock(), 1);
			return;
		}

		if (remainingBurnTime > 0)
			remainingBurnTime--;

		if (activeFuel == FuelType.NORMAL)
			updateBlockState();
		if (remainingBurnTime > 0)
			return;

		if (activeFuel == FuelType.SPECIAL) {
			activeFuel = FuelType.NORMAL;
			remainingBurnTime = maxHeatCapacity / 2;
		} else
			activeFuel = FuelType.NONE;
		updateBlockState();
		notifyUpdate();
	}

	private void tickRotation() {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		float target = 0;
		if (player != null) {
			double dx = player.getX() - (getPos().getX() + 0.5);
			double dz = player.getZ() - (getPos().getZ() + 0.5);
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
		compound.putInt("fuelLevel", activeFuel.ordinal());
		compound.putInt("burnTimeRemaining", remainingBurnTime);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		activeFuel = FuelType.values()[compound.getInt("fuelLevel")];
		remainingBurnTime = compound.getInt("burnTimeRemaining");
		super.read(compound, clientPacket);
	}

	/**
	 * @return true if the heater updated its burn time and a item should be
	 *         consumed
	 */
	boolean tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate) {
		FuelType newFuel = FuelType.NONE;
		int newBurnTime = ForgeHooks.getBurnTime(itemStack);

		if (newBurnTime > 0)
			newFuel = FuelType.NORMAL;
		if (AllItems.BLAZE_CAKE.isIn(itemStack)) {
			newBurnTime = 1000;
			newFuel = FuelType.SPECIAL;
		}

		if (newFuel == FuelType.NONE)
			return false;
		if (newFuel.ordinal() < activeFuel.ordinal())
			return false;
		if (activeFuel == FuelType.SPECIAL && remainingBurnTime > 20)
			return false;

		if (newFuel == activeFuel) {
			if (remainingBurnTime + newBurnTime > maxHeatCapacity && !forceOverflow)
				return false;
			newBurnTime = MathHelper.clamp(remainingBurnTime + newBurnTime, 0, maxHeatCapacity);
		}

		if (simulate)
			return true;

		activeFuel = newFuel;
		remainingBurnTime = newBurnTime;

		if (world.isRemote) {
			HeatLevel level = getHeatLevelFromFuelType(newFuel);
			for (int i = 0; i < 20; i++)
				spawnParticles(level, 1 + (.25 * (i / 4)));
			return true;
		}

		updateBlockState();
		return true;
	}

	public BlazeBurnerBlock.HeatLevel getHeatLevelFromBlock() {
		return BlazeBurnerBlock.getHeatLevelOf(getBlockState());
	}

	public void updateBlockState() {
		HeatLevel inBlockState = getHeatLevelFromBlock();
		HeatLevel inTE = getHeatLevelFromFuelType(activeFuel);
		if (inBlockState == inTE)
			return;
		world.setBlockState(pos, getBlockState().with(BlazeBurnerBlock.HEAT_LEVEL, inTE));
		notifyUpdate();
	}

	protected HeatLevel getHeatLevelFromFuelType(FuelType fuel) {
		HeatLevel level = HeatLevel.SMOULDERING;
		switch (activeFuel) {
		case SPECIAL:
			level = HeatLevel.SEETHING;
			break;
		case NORMAL:
			boolean lowPercent = (double) remainingBurnTime / maxHeatCapacity < 0.1;
			level = lowPercent ? HeatLevel.FADING : HeatLevel.KINDLED;
			break;
		default:
		case NONE:
			break;
		}
		return level;
	}

	private void spawnParticles(HeatLevel heatLevel, double burstMult) {
		if (world == null)
			return;
		if (heatLevel == BlazeBurnerBlock.HeatLevel.NONE)
			return;

		Random r = world.getRandom();
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

	private void spawnParticle(HeatLevel heatLevel, float scale, int avgAge, boolean hot, double speed, double spread) {
		Random random = world.getRandom();
		Vec3d color = randomColor(heatLevel);
		world.addOptionalParticle(
			new CubeParticleData((float) color.x, (float) color.y, (float) color.z, scale, avgAge, hot),
			(double) pos.getX() + 0.5D + (random.nextDouble() * 2.0 - 1D) * spread,
			(double) pos.getY() + 0.6D + (random.nextDouble() / 4.0),
			(double) pos.getZ() + 0.5D + (random.nextDouble() * 2.0 - 1D) * spread, 0.0D, speed, 0.0D);
	}

	private static Vec3d randomColor(BlazeBurnerBlock.HeatLevel heatLevel) {
		if (heatLevel == BlazeBurnerBlock.HeatLevel.NONE)
			return new Vec3d(0, 0, 0);
		return ColorHelper.getRGB(heatParticleColors[heatLevel.ordinal() - 1][(int) (Math.random() * 4)]);
	}

}
