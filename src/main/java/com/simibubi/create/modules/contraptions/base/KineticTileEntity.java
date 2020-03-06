package com.simibubi.create.modules.contraptions.base;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.modules.contraptions.KineticNetwork;
import com.simibubi.create.modules.contraptions.RotationPropagator;
import com.simibubi.create.modules.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.modules.contraptions.base.IRotate.StressImpact;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public abstract class KineticTileEntity extends SmartTileEntity implements ITickableTileEntity {

	protected UUID networkID;
	protected UUID newNetworkID;
	protected float maxStress;
	protected float currentStress;
	protected boolean updateNetwork;

	protected KineticEffectHandler effects;
	protected BlockPos source;
	protected float speed;
	protected boolean overStressed;
	protected boolean initNetwork;

	private int flickerTally;
	private int validationCountdown;

	public KineticTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		effects = new KineticEffectHandler(this);
	}

	@Override
	public void tick() {
		super.tick();
		effects.tick();

		if (world.isRemote)
			return;

		if (validationCountdown-- <= 0) {
			validationCountdown = AllConfigs.SERVER.kinetics.kineticValidationFrequency.get();
			validateKinetics();
		}

		if (getFlickerScore() > 0)
			flickerTally = getFlickerScore() - 1;

		if (initNetwork) {
			initNetwork = false;

			KineticNetwork network = getNetwork();
			if (!network.initialized)
				network.initFromTE(maxStress, currentStress);
			network.addSilently(this);
		}

		if (updateNetwork) {
			updateNetwork = false;

			if (hasNetwork() && !networkID.equals(newNetworkID)) {
				getNetwork().remove(this);
				networkID = null;
				maxStress = currentStress = 0;
				overStressed = false;
			}

			if (newNetworkID != null) {
				networkID = newNetworkID;
				KineticNetwork network = getNetwork();
				network.initialized = true;
				network.add(this);
			}

			sendData();
		}
	}

	private void validateKinetics() {
		if (hasSource()) {
			if (!world.isBlockPresent(source))
				return;

			KineticTileEntity sourceTe = (KineticTileEntity) world.getTileEntity(source);
			if (sourceTe == null || sourceTe.speed == 0) {
				removeSource();
				detachKinetics();
				return;
			}

			if (hasNetwork() && maxStress == 0) {
				for (KineticTileEntity kineticTileEntity : getNetwork().members.keySet()) 
					kineticTileEntity.removeSource();
				return;
			}

			return;
		}

		if (speed != 0) {
			if (getGeneratedSpeed() == 0)
				setSpeed(0);
		}
	}

	public void sync(float maxStress, float currentStress) {
		this.maxStress = maxStress;
		this.currentStress = currentStress;
		boolean overStressed = maxStress < currentStress && StressImpact.isEnabled();
		if (overStressed != this.overStressed) {
			if (speed != 0 && overStressed)
				AllTriggers.triggerForNearbyPlayers(AllTriggers.OVERSTRESSED, world, pos, 8);
			float prevSpeed = getSpeed();
			this.overStressed = overStressed;
			onSpeedChanged(prevSpeed);
			sendData();
		}
	}

	public float getAddedStressCapacity() {
		Map<ResourceLocation, ConfigValue<Double>> capacityMap = AllConfigs.SERVER.kinetics.stressValues.capacities;
		ResourceLocation path = getBlockState().getBlock().getRegistryName();
		if (!capacityMap.containsKey(path))
			return 0;
		return capacityMap.get(path).get().floatValue();
	}

	public float getStressApplied() {
		Map<ResourceLocation, ConfigValue<Double>> stressEntries = AllConfigs.SERVER.kinetics.stressValues.impacts;
		ResourceLocation path = getBlockState().getBlock().getRegistryName();
		if (!stressEntries.containsKey(path))
			return 1;
		return stressEntries.get(path).get().floatValue();
	}

	public void onSpeedChanged(float previousSpeed) {
		boolean fromOrToZero = (previousSpeed == 0) != (getSpeed() == 0);
		boolean directionSwap = !fromOrToZero && Math.signum(previousSpeed) != Math.signum(getSpeed());
		if (fromOrToZero || directionSwap) {
			flickerTally = getFlickerScore() + 5;
		}
	}

	@Override
	public void remove() {
		if (!world.isRemote) {
			if (hasNetwork())
				getNetwork().remove(this);
			detachKinetics();
		}
		super.remove();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putFloat("Speed", speed);

		if (hasSource())
			compound.put("Source", NBTUtil.writeBlockPos(source));

		if (hasNetwork()) {
			compound.putFloat("MaxStress", maxStress);
			compound.putFloat("Stress", currentStress);
			compound.put("Id", NBTUtil.writeUniqueId(networkID));
		}

		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		speed = compound.getFloat("Speed");
		source = null;
		networkID = newNetworkID = null;
		overStressed = false;

		if (compound.contains("Source"))
			source = NBTUtil.readBlockPos(compound.getCompound("Source"));

		if (compound.contains("Id")) {
			maxStress = compound.getFloat("MaxStress");
			currentStress = compound.getFloat("Stress");
			overStressed = maxStress < currentStress && StressImpact.isEnabled();
			networkID = NBTUtil.readUniqueId(compound.getCompound("Id"));
			newNetworkID = networkID;
			initNetwork = true;
		}

		super.read(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		boolean overStressedBefore = overStressed;
		super.readClientUpdate(tag);
		if (overStressedBefore != overStressed && speed != 0)
			effects.triggerOverStressedEffect();
	}

	public boolean isSource() {
		return getGeneratedSpeed() != 0;
	}

	public float getSpeed() {
		if (overStressed)
			return 0;
		return getTheoreticalSpeed();
	}

	public float getTheoreticalSpeed() {
		return speed;
	}

	public float getGeneratedSpeed() {
		return 0;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public boolean hasSource() {
		return source != null;
	}

	public BlockPos getSource() {
		return source;
	}

	public void setSource(BlockPos source) {
		this.source = source;
		if (world == null || world.isRemote)
			return;

		KineticTileEntity sourceTe = (KineticTileEntity) world.getTileEntity(source);
		if (sourceTe == null) {
			removeSource();
			return;
		}

		newNetworkID = sourceTe.newNetworkID;
		updateNetwork = true;
	}

	public void removeSource() {
		source = null;
		newNetworkID = null;
		updateNetwork = true;
		float prevSpeed = getSpeed();
		setSpeed(0);
		onSpeedChanged(prevSpeed);
	}

	public KineticNetwork getNetwork() {
		return Create.torquePropagator.getNetworkFor(this);
	}

	public boolean hasNetwork() {
		return networkID != null;
	}

	public boolean canOverPower(KineticTileEntity other) {
		return newNetworkID != null && !newNetworkID.equals(other.newNetworkID);
	}

	public void attachKinetics() {
		RotationPropagator.handleAdded(world, pos, this);
	}

	public void detachKinetics() {
		RotationPropagator.handleRemoved(world, pos, this);
	}

	public UUID getNetworkID() {
		return networkID;
	}

	public void setNetworkID(UUID networkID) {
		this.networkID = networkID;
	}

	public boolean isSpeedRequirementFulfilled() {
		BlockState state = getBlockState();
		if (!(getBlockState().getBlock() instanceof IRotate))
			return true;
		IRotate def = (IRotate) state.getBlock();
		SpeedLevel minimumRequiredSpeedLevel = def.getMinimumRequiredSpeedLevel();
		if (minimumRequiredSpeedLevel == null)
			return true;
		if (minimumRequiredSpeedLevel == SpeedLevel.MEDIUM)
			return Math.abs(getSpeed()) >= AllConfigs.SERVER.kinetics.mediumSpeed.get();
		if (minimumRequiredSpeedLevel == SpeedLevel.FAST)
			return Math.abs(getSpeed()) >= AllConfigs.SERVER.kinetics.fastSpeed.get();
		return true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	public int getFlickerScore() {
		return flickerTally;
	}

}
