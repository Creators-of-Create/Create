package com.simibubi.create.content.contraptions.processing;

import java.util.List;
import java.util.Random;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.deployer.DeployerFakePlayer;
import com.simibubi.create.content.contraptions.particle.HeaterParticleData;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ForgeHooks;

public class HeaterTileEntity extends SmartTileEntity {
	private int fuelLevel;
	private int burnTimeRemaining;
	private static final int maxHeatCapacity = 10000;

	public HeaterTileEntity(TileEntityType<? extends HeaterTileEntity> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		fuelLevel = 0;
		burnTimeRemaining = 0;
		setLazyTickRate(20);
	}

	@Override
	public void tick() {
		super.tick();
		if (burnTimeRemaining > 0) {
			burnTimeRemaining--;
			if (burnTimeRemaining <= 0 && fuelLevel > 1) {
				fuelLevel--;
				burnTimeRemaining = maxHeatCapacity / 2;
			}
			updateHeatLevel();
			markDirty();
		}
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		spawnParticles(ParticleTypes.SMOKE);
		int heatLevel = getHeatLevel();
		if (heatLevel >= 2)
			spawnParticles(ParticleTypes.FLAME);
		if (heatLevel > 3) {
			spawnParticles(new HeaterParticleData(0.3f, 0.3f, 1f));
		}
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("fuelLevel", fuelLevel);
		compound.putInt("burnTimeRemaining", burnTimeRemaining);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		fuelLevel = compound.getInt("fuelLevel");
		burnTimeRemaining = compound.getInt("burnTimeRemaining");
		super.read(compound);
		if (fuelLevel == 0)
			burnTimeRemaining = 0;
		updateHeatLevel();
	}

	boolean tryUpdateFuel(ItemStack itemStack, PlayerEntity player) {
		boolean specialFuelUsed = itemStack.getItem() == AllItems.FUEL_PELLET.get();
		int burnTime =
			itemStack.getItem() == Items.EGG ? 150 : (specialFuelUsed ? 1000 : ForgeHooks.getBurnTime(itemStack));
		int newFuelLevel = (specialFuelUsed ? 2 : 1);
		if (burnTime <= 0 || newFuelLevel < fuelLevel)
			return false;
		if (newFuelLevel > this.fuelLevel) {
			fuelLevel = newFuelLevel;
			burnTimeRemaining = burnTime;
		} else {
			if (burnTimeRemaining + burnTime > maxHeatCapacity && player instanceof DeployerFakePlayer)
				return false;
			burnTimeRemaining = MathHelper.clamp(burnTimeRemaining + burnTime, 0, maxHeatCapacity);
		}
		updateHeatLevel();
		return true;
	}

	public int getHeatLevel() {
		return HeaterBlock.getHeaterLevel(getBlockState());
	}

	private void updateHeatLevel() {
		if (fuelLevel == 2)
			HeaterBlock.setBlazeLevel(world, pos, 4);
		else if (fuelLevel == 0 || burnTimeRemaining <= 0)
			HeaterBlock.setBlazeLevel(world, pos, 1);
		else {
			HeaterBlock.setBlazeLevel(world, pos, (double) burnTimeRemaining / maxHeatCapacity > 0.1 ? 3 : 2);
		}
	}

	private void spawnParticles(IParticleData basicparticletype) {
		if (world == null)
			return;
		Random random = world.getRandom();
		world.addOptionalParticle(basicparticletype, true,
			(double) pos.getX() + 0.5D + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1),
			(double) pos.getY() + random.nextDouble() + random.nextDouble(),
			(double) pos.getZ() + 0.5D + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1), 0.0D,
			0.07D, 0.0D);
	}
}
