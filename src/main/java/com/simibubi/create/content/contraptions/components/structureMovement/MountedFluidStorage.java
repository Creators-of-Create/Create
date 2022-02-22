package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionFluidPacket;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity.CreativeSmartFluidTank;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.PacketDistributor;

public class MountedFluidStorage {

	SmartFluidTank tank;
	private boolean valid;
	private BlockEntity te;

	private int packetCooldown = 0;
	private boolean sendPacket = false;

	public static boolean canUseAsStorage(BlockEntity te) {
		if (te instanceof FluidTankTileEntity)
			return ((FluidTankTileEntity) te).isController();
		return false;
	}

	public MountedFluidStorage(BlockEntity te) {
		assignTileEntity(te);
	}

	public void assignTileEntity(BlockEntity te) {
		this.te = te;
		tank = createMountedTank(te);
	}

	private SmartFluidTank createMountedTank(BlockEntity te) {
		if (te instanceof CreativeFluidTankTileEntity)
			return new CreativeSmartFluidTank(
				((FluidTankTileEntity) te).getTotalTankSize() * FluidTankTileEntity.getCapacityMultiplier(), $ -> {
				});
		if (te instanceof FluidTankTileEntity)
			return new SmartFluidTank(
				((FluidTankTileEntity) te).getTotalTankSize() * FluidTankTileEntity.getCapacityMultiplier(),
				this::onFluidStackChanged);
		return null;
	}

	public void tick(Entity entity, BlockPos pos, boolean isRemote) {
		if (!isRemote) {
			if (packetCooldown > 0)
				packetCooldown--;
			else if (sendPacket) {
				sendPacket = false;
				AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
					new ContraptionFluidPacket(entity.getId(), pos, tank.getFluid()));
				packetCooldown = 8;
			}
			return;
		}

		if (!(te instanceof FluidTankTileEntity))
			return;
		FluidTankTileEntity tank = (FluidTankTileEntity) te;
		tank.getFluidLevel()
			.tickChaser();
	}

	public void updateFluid(FluidStack fluid) {
		tank.setFluid(fluid);
		if (!(te instanceof FluidTankTileEntity))
			return;
		float fillState = tank.getFluidAmount() / (float) tank.getCapacity();
		FluidTankTileEntity tank = (FluidTankTileEntity) te;
		if (tank.getFluidLevel() == null)
			tank.setFluidLevel(LerpedFloat.linear()
				.startWithValue(fillState));
		tank.getFluidLevel()
			.chase(fillState, 0.5, Chaser.EXP);
		IFluidTank tankInventory = tank.getTankInventory();
		if (tankInventory instanceof SmartFluidTank)
			((SmartFluidTank) tankInventory).setFluid(fluid);
	}

	public void removeStorageFromWorld() {
		valid = false;
		if (te == null)
			return;

		IFluidHandler teHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			.orElse(null);
		if (!(teHandler instanceof SmartFluidTank))
			return;
		SmartFluidTank smartTank = (SmartFluidTank) teHandler;
		tank.setFluid(smartTank.getFluid());
		sendPacket = false;
		valid = true;
	}

	private void onFluidStackChanged(FluidStack fs) {
		sendPacket = true;
	}

	public void addStorageToWorld(BlockEntity te) {
		if (tank instanceof CreativeSmartFluidTank)
			return;

		LazyOptional<IFluidHandler> capability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		IFluidHandler teHandler = capability.orElse(null);
		if (!(teHandler instanceof SmartFluidTank))
			return;

		SmartFluidTank inv = (SmartFluidTank) teHandler;
		inv.setFluid(tank.getFluid()
			.copy());
	}

	public IFluidHandler getFluidHandler() {
		return tank;
	}

	public CompoundTag serialize() {
		if (!valid)
			return null;
		CompoundTag tag = tank.writeToNBT(new CompoundTag());
		tag.putInt("Capacity", tank.getCapacity());

		if (tank instanceof CreativeSmartFluidTank) {
			NBTHelper.putMarker(tag, "Bottomless");
			tag.put("ProvidedStack", tank.getFluid()
				.writeToNBT(new CompoundTag()));
		}
		return tag;
	}

	public static MountedFluidStorage deserialize(CompoundTag nbt) {
		MountedFluidStorage storage = new MountedFluidStorage(null);
		if (nbt == null)
			return storage;

		int capacity = nbt.getInt("Capacity");
		storage.tank = new SmartFluidTank(capacity, storage::onFluidStackChanged);
		storage.valid = true;

		if (nbt.contains("Bottomless")) {
			FluidStack providedStack = FluidStack.loadFluidStackFromNBT(nbt.getCompound("ProvidedStack"));
			CreativeSmartFluidTank creativeSmartFluidTank = new CreativeSmartFluidTank(capacity, $ -> {
			});
			creativeSmartFluidTank.setContainedFluid(providedStack);
			storage.tank = creativeSmartFluidTank;
			return storage;
		}

		storage.tank.readFromNBT(nbt);
		return storage;
	}

	public boolean isValid() {
		return valid;
	}

}
