package com.simibubi.create.content.contraptions;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.sync.ContraptionFluidPacket;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity.CreativeSmartFluidTank;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.PacketDistributor;

public class MountedFluidStorage {

	SmartFluidTank tank;
	private boolean valid;
	private BlockEntity blockEntity;

	private int packetCooldown = 0;
	private boolean sendPacket = false;

	public static boolean canUseAsStorage(BlockEntity be) {
		if (be instanceof FluidTankBlockEntity)
			return ((FluidTankBlockEntity) be).isController();
		return false;
	}

	public MountedFluidStorage(BlockEntity be) {
		assignBlockEntity(be);
	}

	public void assignBlockEntity(BlockEntity be) {
		this.blockEntity = be;
		tank = createMountedTank(be);
	}

	private SmartFluidTank createMountedTank(BlockEntity be) {
		if (be instanceof CreativeFluidTankBlockEntity)
			return new CreativeSmartFluidTank(
				((FluidTankBlockEntity) be).getTotalTankSize() * FluidTankBlockEntity.getCapacityMultiplier(), $ -> {
				});
		if (be instanceof FluidTankBlockEntity)
			return new SmartFluidTank(
				((FluidTankBlockEntity) be).getTotalTankSize() * FluidTankBlockEntity.getCapacityMultiplier(),
				this::onFluidStackChanged);
		return null;
	}

	public void tick(Entity entity, BlockPos pos, boolean isRemote) {
		if (!isRemote) {
			if (packetCooldown > 0)
				packetCooldown--;
			else if (sendPacket) {
				sendPacket = false;
				AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
					new ContraptionFluidPacket(entity.getId(), pos, tank.getFluid()));
				packetCooldown = 8;
			}
			return;
		}

		if (!(blockEntity instanceof FluidTankBlockEntity))
			return;
		FluidTankBlockEntity tank = (FluidTankBlockEntity) blockEntity;
		tank.getFluidLevel()
			.tickChaser();
	}

	public void updateFluid(FluidStack fluid) {
		tank.setFluid(fluid);
		if (!(blockEntity instanceof FluidTankBlockEntity))
			return;
		float fillState = tank.getFluidAmount() / (float) tank.getCapacity();
		FluidTankBlockEntity tank = (FluidTankBlockEntity) blockEntity;
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
		if (blockEntity == null)
			return;

		IFluidHandler teHandler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER)
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

	public void addStorageToWorld(BlockEntity be) {
		if (tank instanceof CreativeSmartFluidTank)
			return;

		LazyOptional<IFluidHandler> capability = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
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
