package com.simibubi.create.modules.contraptions.base;

import static net.minecraft.util.text.TextFormatting.GREEN;
import static net.minecraft.util.text.TextFormatting.WHITE;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.KineticNetwork;
import com.simibubi.create.modules.contraptions.RotationPropagator;
import com.simibubi.create.modules.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.modules.contraptions.particle.RotationIndicatorParticleData;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public abstract class KineticTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	int particleSpawnCountdown;

	// Speed related
	public float speed;
	protected Optional<BlockPos> source;
	public boolean reActivateSource;
	public int speedChangeCounter;

	// Torque related
	public float maxStress;
	public float currentStress;
	protected boolean overStressed;

	public UUID networkID;
	public UUID newNetworkID;
	public boolean updateNetwork;
	protected boolean initNetwork;

	public KineticTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		speed = 0;
		source = Optional.empty();
	}

	public void sync(float maxStress, float currentStress) {
		this.maxStress = maxStress;
		this.currentStress = currentStress;
		boolean overStressed = maxStress < currentStress;
		if (overStressed != this.overStressed) {
			this.overStressed = overStressed;
			onSpeedChanged();
			sendData();
		}
	}

	public float getAddedStressCapacity() {
		Map<ResourceLocation, DoubleValue> capacityMap = CreateConfig.parameters.stressCapacityEntries;
		ResourceLocation path = getBlockState().getBlock().getRegistryName();
		if (!capacityMap.containsKey(path))
			return 0;
		return capacityMap.get(path).get().floatValue();
	}

	public float getStressApplied() {
		Map<ResourceLocation, DoubleValue> stressEntries = CreateConfig.parameters.stressEntries;
		ResourceLocation path = getBlockState().getBlock().getRegistryName();
		if (!stressEntries.containsKey(path))
			return 1;
		return stressEntries.get(path).get().floatValue();
	}

	protected void notifyStressChange(float stress) {
		getNetwork().updateStressFor(this, stress);
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	public void onSpeedChanged() {
		speedChangeCounter += 5;
	}

	@Override
	public void remove() {
		if (world.isRemote) {
			super.remove();
			return;
		}
		if (hasNetwork()) {
			getNetwork().remove(this);
		}
		RotationPropagator.handleRemoved(getWorld(), getPos(), this);
		super.remove();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putFloat("Speed", speed);
		if (hasSource())
			compound.put("Source", NBTUtil.writeBlockPos(getSource()));

		if (hasNetwork()) {
			compound.putFloat("MaxStress", maxStress);
			compound.putFloat("Stress", currentStress);
			compound.put("Id", NBTUtil.writeUniqueId(getNetworkID()));
		}

		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		setSpeed(compound.getFloat("Speed"));
		setSource(null);
		if (compound.contains("Source")) {
			CompoundNBT tagSource = compound.getCompound("Source");
			setSource(NBTUtil.readBlockPos(tagSource));
		}

		if (compound.contains("Id")) {
			maxStress = compound.getFloat("MaxStress");
			currentStress = compound.getFloat("Stress");
			overStressed = maxStress < currentStress;
			setNetworkID(NBTUtil.readUniqueId(compound.getCompound("Id")));
			newNetworkID = networkID;
			initNetwork = true;
		} else {
			networkID = newNetworkID = null;
			overStressed = false;
		}

		super.read(compound);
	}

	public boolean isSource() {
		return getGeneratedSpeed() != 0;
	}

	public float getSpeed() {
		if (overStressed)
			return 0;
		return speed;
	}

	public float getGeneratedSpeed() {
		return 0;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
		if (hasWorld() && speed != 0 && world.isRemote) {
			Random r = getWorld().rand;
			for (int i = 0; i < 2; i++) {
				float x = getPos().getX() + (r.nextFloat() - .5f) / 2f + .5f;
				float y = getPos().getY() + (r.nextFloat() - .5f) / 2f + .5f;
				float z = getPos().getZ() + (r.nextFloat() - .5f) / 2f + .5f;
				this.getWorld().addParticle(new RedstoneParticleData(1, 1, 1, 1), x, y, z, 0, 0, 0);
			}
		}
	}

	public boolean hasSource() {
		return source.isPresent();
	}

	public BlockPos getSource() {
		return source.get();
	}

	public Direction getSourceFacing() {
		BlockPos source = getSource().subtract(getPos());
		return Direction.getFacingFromVector(source.getX(), source.getY(), source.getZ());
	}

	public void setSource(BlockPos source) {
		this.source = Optional.ofNullable(source);

		if (world == null || world.isRemote)
			return;
		if (source == null)
			return;
		KineticTileEntity sourceTe = (KineticTileEntity) world.getTileEntity(source);
		if (sourceTe == null)
			return;

		if (reActivateSource && Math.abs(sourceTe.getSpeed()) >= Math.abs(getGeneratedSpeed())) {
			reActivateSource = false;
		}

		newNetworkID = sourceTe.newNetworkID;
		updateNetwork = true;
	}

	public void removeSource() {
		if (hasSource() && isSource())
			reActivateSource = true;

		this.source = Optional.empty();
		newNetworkID = null;
		updateNetwork = true;
		setSpeed(0);
		onSpeedChanged();
	}

	public KineticNetwork getNetwork() {
		return Create.torquePropagator.getNetworkFor(this);
	}

	public boolean hasNetwork() {
		return networkID != null;
	}

	public void applyNewSpeed(float speed) {
		detachKinetics();
		this.speed = speed;
		attachKinetics();
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

	/**
	 * Callback for source blocks to re-apply their speed when an overpowering
	 * source is removed
	 */
	public void reActivateSource() {

	}

	@Override
	public void tick() {
		if (world.isRemote)
			return;

		if (speedChangeCounter > 25)
			world.destroyBlock(pos, true);
		if (speedChangeCounter > 0)
			speedChangeCounter--;

		if (particleSpawnCountdown > 0)
			if (--particleSpawnCountdown == 0)
				spawnRotationIndicators();

		if (initNetwork) {
			initNetwork = false;

			KineticNetwork network = getNetwork();
			if (!network.initialized)
				network.initFromTE(this);
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

		if (reActivateSource) {
			reActivateSource();
			reActivateSource = false;
		}
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
			return Math.abs(getSpeed()) >= CreateConfig.parameters.mediumSpeed.get();
		if (minimumRequiredSpeedLevel == SpeedLevel.FAST)
			return Math.abs(getSpeed()) >= CreateConfig.parameters.fastSpeed.get();
		return true;
	}

	public void addDebugInformation(List<String> lines) {
		lines.add("Speed: " + GREEN + speed);
		lines.add("Cost: " + GREEN + getStressApplied() + WHITE + "/" + GREEN + getAddedStressCapacity());
		lines.add("Stress: " + GREEN + currentStress + WHITE + "/" + GREEN + maxStress);
	}

	public void queueRotationIndicators() {
		// wait a few ticks for network jamming etc
		particleSpawnCountdown = 2;
	}

	protected void spawnRotationIndicators() {
		if (getSpeed() == 0)
			return;

		BlockState state = getBlockState();
		Block block = state.getBlock();
		if (!(block instanceof KineticBlock))
			return;

		KineticBlock kb = (KineticBlock) block;
		float radius1 = kb.getParticleInitialRadius();
		float radius2 = kb.getParticleTargetRadius();

		Axis axis = kb.getRotationAxis(state);
		if (axis == null)
			return;
		char axisChar = axis.name().charAt(0);
		Vec3d vec = VecHelper.getCenterOf(pos);

		SpeedLevel speedLevel = SpeedLevel.of(getSpeed());
		int color = speedLevel.getColor();
		int particleSpeed = speedLevel.getParticleSpeed();
		particleSpeed *= Math.signum(getSpeed());

		if (getWorld() instanceof ServerWorld) {
			RotationIndicatorParticleData particleData = new RotationIndicatorParticleData(color, particleSpeed,
					radius1, radius2, 10, axisChar);
			((ServerWorld) getWorld()).spawnParticle(particleData, vec.x, vec.y, vec.z, 20, 0, 0, 0, 1);
		}
	}

}
