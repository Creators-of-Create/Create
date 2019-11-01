package com.simibubi.create.modules.contraptions.base;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.KineticNetwork;
import com.simibubi.create.modules.contraptions.RotationPropagator;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public abstract class KineticTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	public float speed;
	protected Optional<BlockPos> source;
	public boolean reActivateSource;

	public float maxStress;
	public float currentStress;
	public UUID networkID;
	protected boolean overStressed;
	protected boolean initNetwork;

	public KineticTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		speed = 0;
		source = Optional.empty();
	}

	public void sync(UUID networkID, float maxStress, float currentStress) {
		this.setNetworkID(networkID);
		this.maxStress = maxStress;
		this.currentStress = currentStress;
		boolean overStressed = maxStress < currentStress;
		if (overStressed != this.overStressed) {

			Lang.debugChat(getType().getRegistryName().getPath() + " jammed (" + currentStress + "/" + maxStress + ")");

			this.overStressed = overStressed;
			sendData();
		}
	}

	public float getAddedStressCapacity() {
		return 0;
	}

	public float getStressApplied() {
		return isSource() ? 0 : 1;
	}

	protected void notifyStressChange(float diff) {
		KineticNetwork network = getNetwork();
		network.setCurrentStress(network.getCurrentStress() + diff);
		network.sync();
	}

	protected void notifyStressCapacityChange(float capacity) {
		getNetwork().updateCapacityFor(this, capacity);
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	public void onSpeedChanged() {
//		if (isSource() && !world.isRemote) {
//			if (networkID == null)
//				getNetwork().add(this);
//		}
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
			initNetwork = true;
		}

		super.read(compound);
	}

	public boolean isSource() {
		return false;
	}

	public float getSpeed() {
		if (overStressed)
			return 0;
		return speed;
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
		if (hasNetwork()) {
			getNetwork().remove(this);
			networkID = null;
		}
		if (source == null)
			return;
		KineticTileEntity sourceTe = (KineticTileEntity) world.getTileEntity(source);
		if (sourceTe == null)
			return;
		Create.torquePropagator.getNetworkFor(sourceTe).add(this);
	}

	public void removeSource() {
		if (hasSource() && isSource())
			reActivateSource = true;

		this.source = Optional.empty();

		if (hasNetwork() && !isSource()) {
			getNetwork().remove(this);
			networkID = null;
		}

		setSpeed(0);
		onSpeedChanged();
	}

	public KineticNetwork getNetwork() {
		KineticNetwork networkFor = Create.torquePropagator.getNetworkFor(this);
		if (!networkFor.initialized) {
			networkFor.add(this);
			networkFor.initialized = true;
		}
		return networkFor;
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
		if (reActivateSource) {
			reActivateSource();
			reActivateSource = false;
		}

		if (initNetwork) {
			initNetwork = false;
			KineticNetwork network = getNetwork();
			if (network.initialized) {
				network.addSilently(this);
			} else {
				network.initFromTE(this);
			}
		}
	}

}
