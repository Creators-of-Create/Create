package com.simibubi.create.content.contraptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.simibubi.create.content.contraptions.Contraption.ContraptionInvWrapper;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class MountedStorageManager {

	protected ContraptionInvWrapper inventory;
	protected ContraptionInvWrapper fuelInventory;
	protected CombinedTankWrapper fluidInventory;
	protected Map<BlockPos, MountedStorage> storage;
	protected Map<BlockPos, MountedFluidStorage> fluidStorage;

	public MountedStorageManager() {
		storage = new TreeMap<>();
		fluidStorage = new TreeMap<>();
	}

	public void entityTick(AbstractContraptionEntity entity) {
		fluidStorage.forEach((pos, mfs) -> mfs.tick(entity, pos, entity.level.isClientSide));
	}

	public void createHandlers() {
		Collection<MountedStorage> itemHandlers = storage.values();

		inventory = wrapItems(itemHandlers.stream()
			.map(MountedStorage::getItemHandler)
			.toList(), false);

		fuelInventory = wrapItems(itemHandlers.stream()
			.filter(MountedStorage::canUseForFuel)
			.map(MountedStorage::getItemHandler)
			.toList(), true);

		fluidInventory = wrapFluids(fluidStorage.values()
			.stream()
			.map(MountedFluidStorage::getFluidHandler)
			.collect(Collectors.toList()));
	}

	protected ContraptionInvWrapper wrapItems(Collection<IItemHandlerModifiable> list, boolean fuel) {
		return new ContraptionInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class));
	}

	protected CombinedTankWrapper wrapFluids(Collection<IFluidHandler> list) {
		return new CombinedTankWrapper(Arrays.copyOf(list.toArray(), list.size(), IFluidHandler[].class));
	}

	public void addBlock(BlockPos localPos, BlockEntity be) {
		if (be != null && MountedStorage.canUseAsStorage(be))
			storage.put(localPos, new MountedStorage(be));
		if (be != null && MountedFluidStorage.canUseAsStorage(be))
			fluidStorage.put(localPos, new MountedFluidStorage(be));
	}

	public void read(CompoundTag nbt, Map<BlockPos, BlockEntity> presentBlockEntities, boolean clientPacket) {
		storage.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Storage", Tag.TAG_COMPOUND), c -> storage
			.put(NbtUtils.readBlockPos(c.getCompound("Pos")), MountedStorage.deserialize(c.getCompound("Data"))));

		fluidStorage.clear();
		NBTHelper.iterateCompoundList(nbt.getList("FluidStorage", Tag.TAG_COMPOUND), c -> fluidStorage
			.put(NbtUtils.readBlockPos(c.getCompound("Pos")), MountedFluidStorage.deserialize(c.getCompound("Data"))));

		if (clientPacket && presentBlockEntities != null)
			bindTanks(presentBlockEntities);

		List<IItemHandlerModifiable> handlers = new ArrayList<>();
		List<IItemHandlerModifiable> fuelHandlers = new ArrayList<>();
		for (MountedStorage mountedStorage : storage.values()) {
			IItemHandlerModifiable itemHandler = mountedStorage.getItemHandler();
			handlers.add(itemHandler);
			if (mountedStorage.canUseForFuel())
				fuelHandlers.add(itemHandler);
		}

		inventory = wrapItems(handlers, false);
		fuelInventory = wrapItems(fuelHandlers, true);
		fluidInventory = wrapFluids(fluidStorage.values()
			.stream()
			.map(MountedFluidStorage::getFluidHandler)
			.toList());
	}

	public void bindTanks(Map<BlockPos, BlockEntity> presentBlockEntities) {
		fluidStorage.forEach((pos, mfs) -> {
			BlockEntity blockEntity = presentBlockEntities.get(pos);
			if (!(blockEntity instanceof FluidTankBlockEntity))
				return;
			FluidTankBlockEntity tank = (FluidTankBlockEntity) blockEntity;
			IFluidTank tankInventory = tank.getTankInventory();
			if (tankInventory instanceof FluidTank)
				((FluidTank) tankInventory).setFluid(mfs.tank.getFluid());
			tank.getFluidLevel()
				.startWithValue(tank.getFillState());
			mfs.assignBlockEntity(tank);
		});
	}

	public void write(CompoundTag nbt, boolean clientPacket) {
		ListTag storageNBT = new ListTag();
		if (!clientPacket)
			for (BlockPos pos : storage.keySet()) {
				CompoundTag c = new CompoundTag();
				MountedStorage mountedStorage = storage.get(pos);
				if (!mountedStorage.isValid())
					continue;
				c.put("Pos", NbtUtils.writeBlockPos(pos));
				c.put("Data", mountedStorage.serialize());
				storageNBT.add(c);
			}

		ListTag fluidStorageNBT = new ListTag();
		for (BlockPos pos : fluidStorage.keySet()) {
			CompoundTag c = new CompoundTag();
			MountedFluidStorage mountedStorage = fluidStorage.get(pos);
			if (!mountedStorage.isValid())
				continue;
			c.put("Pos", NbtUtils.writeBlockPos(pos));
			c.put("Data", mountedStorage.serialize());
			fluidStorageNBT.add(c);
		}

		nbt.put("Storage", storageNBT);
		nbt.put("FluidStorage", fluidStorageNBT);
	}

	public void removeStorageFromWorld() {
		storage.values()
			.forEach(MountedStorage::removeStorageFromWorld);
		fluidStorage.values()
			.forEach(MountedFluidStorage::removeStorageFromWorld);
	}

	public void addStorageToWorld(StructureBlockInfo block, BlockEntity blockEntity) {
		if (storage.containsKey(block.pos)) {
			MountedStorage mountedStorage = storage.get(block.pos);
			if (mountedStorage.isValid())
				mountedStorage.addStorageToWorld(blockEntity);
		}

		if (fluidStorage.containsKey(block.pos)) {
			MountedFluidStorage mountedStorage = fluidStorage.get(block.pos);
			if (mountedStorage.isValid())
				mountedStorage.addStorageToWorld(blockEntity);
		}
	}

	public void clear() {
		for (int i = 0; i < inventory.getSlots(); i++)
			if (!inventory.isSlotExternal(i))
				inventory.setStackInSlot(i, ItemStack.EMPTY);
		for (int i = 0; i < fluidInventory.getTanks(); i++)
			fluidInventory.drain(fluidInventory.getFluidInTank(i), FluidAction.EXECUTE);
	}

	public void updateContainedFluid(BlockPos localPos, FluidStack containedFluid) {
		MountedFluidStorage mountedFluidStorage = fluidStorage.get(localPos);
		if (mountedFluidStorage != null)
			mountedFluidStorage.updateFluid(containedFluid);
	}

	public void attachExternal(IItemHandlerModifiable externalStorage) {
		inventory = new ContraptionInvWrapper(externalStorage, inventory);
		fuelInventory = new ContraptionInvWrapper(externalStorage, fuelInventory);
	}

	public IItemHandlerModifiable getItems() {
		return inventory;
	}

	public IItemHandlerModifiable getFuelItems() {
		return fuelInventory;
	}

	public IFluidHandler getFluids() {
		return fluidInventory;
	}

	public boolean handlePlayerStorageInteraction(Contraption contraption, Player player, BlockPos localPos) {
		if (player.level.isClientSide()) {
			BlockEntity localBE = contraption.presentBlockEntities.get(localPos);
			return MountedStorage.canUseAsStorage(localBE);
		}

		MountedStorageManager storageManager = contraption.getStorageForSpawnPacket();
		MountedStorage storage = storageManager.storage.get(localPos);
		if (storage == null || storage.getItemHandler() == null)
			return false;
		IItemHandlerModifiable handler = storage.getItemHandler();

		StructureBlockInfo info = contraption.getBlocks()
			.get(localPos);
		if (info != null && info.state.hasProperty(ChestBlock.TYPE)) {
			ChestType chestType = info.state.getValue(ChestBlock.TYPE);
			Direction facing = info.state.getOptionalValue(ChestBlock.FACING)
				.orElse(Direction.SOUTH);
			Direction connectedDirection =
				chestType == ChestType.LEFT ? facing.getClockWise() : facing.getCounterClockWise();

			if (chestType != ChestType.SINGLE) {
				MountedStorage storage2 = storageManager.storage.get(localPos.relative(connectedDirection));
				if (storage2 != null && storage2.getItemHandler() != null)
					handler = chestType == ChestType.RIGHT ? new CombinedInvWrapper(handler, storage2.getItemHandler())
						: new CombinedInvWrapper(storage2.getItemHandler(), handler);
			}
		}

		int slotCount = handler.getSlots();
		if (slotCount == 0)
			return false;
		if (slotCount % 9 != 0)
			return false;

		Supplier<Boolean> stillValid = () -> contraption.entity.isAlive()
			&& player.distanceToSqr(contraption.entity.toGlobalVector(Vec3.atCenterOf(localPos), 0)) < 64;
		Component name = info != null ? info.state.getBlock()
			.getName() : Components.literal("Container");
		player.openMenu(MountedStorageInteraction.createMenuProvider(name, handler, slotCount, stillValid));

		Vec3 soundPos = contraption.entity.toGlobalVector(Vec3.atCenterOf(localPos), 0);
		player.level.playSound(null, new BlockPos(soundPos), SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.75f, 1f);
		return true;
	}

}
