package com.simibubi.create.content.trains.station;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.simibubi.create.compat.computercraft.events.PackageEvent;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class GlobalStation extends SingleBlockEntityEdgePoint {

	public String name;
	public WeakReference<Train> nearestTrain;
	public boolean assembling;

	public Map<BlockPos, GlobalPackagePort> connectedPorts;

	public GlobalStation() {
		name = "Track Station";
		nearestTrain = new WeakReference<>(null);
		connectedPorts = new HashMap<>();
	}

	@Override
	public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
		super.blockEntityAdded(blockEntity, front);
		BlockState state = blockEntity.getBlockState();
		assembling =
			state != null && state.hasProperty(StationBlock.ASSEMBLING) && state.getValue(StationBlock.ASSEMBLING);
	}

	@Override
	public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean migration, DimensionPalette dimensions) {
		super.read(nbt, registries, migration, dimensions);
		name = nbt.getString("Name");
		assembling = nbt.getBoolean("Assembling");
		nearestTrain = new WeakReference<>(null);

		connectedPorts.clear();
		ListTag portList = nbt.getList("Ports", Tag.TAG_COMPOUND);
		NBTHelper.iterateCompoundList(portList, c -> {
			GlobalPackagePort port = new GlobalPackagePort();
			port.address = c.getString("Address");
			port.offlineBuffer.deserializeNBT(registries, c.getCompound("OfflineBuffer"));
			port.primed = c.getBoolean("Primed");
			connectedPorts.put(NBTHelper.readBlockPos(c, "Pos"), port);
		});
	}

	@Override
	public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.read(buffer, dimensions);
		name = buffer.readUtf();
		assembling = buffer.readBoolean();
		if (buffer.readBoolean())
			blockEntityPos = buffer.readBlockPos();
	}

	@Override
	public void write(CompoundTag nbt, HolderLookup.Provider registries, DimensionPalette dimensions) {
		super.write(nbt, registries, dimensions);
		nbt.putString("Name", name);
		nbt.putBoolean("Assembling", assembling);

		nbt.put("Ports", NBTHelper.writeCompoundList(connectedPorts.entrySet(), e -> {
			CompoundTag c = new CompoundTag();
			c.putString("Address", e.getValue().address);
			c.put("OfflineBuffer", e.getValue().offlineBuffer.serializeNBT(registries));
			c.putBoolean("Primed", e.getValue().primed);
			c.put("Pos", NbtUtils.writeBlockPos(e.getKey()));
			return c;
		}));
	}

	@Override
	public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.write(buffer, dimensions);
		buffer.writeUtf(name);
		buffer.writeBoolean(assembling);
		buffer.writeBoolean(blockEntityPos != null);
		if (blockEntityPos != null)
			buffer.writeBlockPos(blockEntityPos);
	}

	public boolean canApproachFrom(TrackNode side) {
		return isPrimary(side) && !assembling;
	}

	@Override
	public boolean canNavigateVia(TrackNode side) {
		return super.canNavigateVia(side) && !assembling;
	}

	public void reserveFor(Train train) {
		Train nearestTrain = getNearestTrain();
		if (nearestTrain == null
			|| nearestTrain.navigation.distanceToDestination > train.navigation.distanceToDestination)
			this.nearestTrain = new WeakReference<>(train);
	}

	public void cancelReservation(Train train) {
		if (nearestTrain.get() == train)
			nearestTrain = new WeakReference<>(null);
	}

	public void trainDeparted(Train train) {
		cancelReservation(train);
	}

	@Nullable
	public Train getPresentTrain() {
		Train nearestTrain = getNearestTrain();
		if (nearestTrain == null || nearestTrain.getCurrentStation() != this)
			return null;
		return nearestTrain;
	}

	@Nullable
	public Train getImminentTrain() {
		Train nearestTrain = getNearestTrain();
		if (nearestTrain == null)
			return nearestTrain;
		if (nearestTrain.getCurrentStation() == this)
			return nearestTrain;
		if (!nearestTrain.navigation.isActive())
			return null;
		if (nearestTrain.navigation.distanceToDestination > 30)
			return null;
		return nearestTrain;
	}

	@Nullable
	public Train getNearestTrain() {
		return this.nearestTrain.get();
	}

	public void runMailTransfer() {
		Train train = getPresentTrain();
		if (train == null || connectedPorts.isEmpty())
			return;

		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		Level level = server.getLevel(getBlockEntityDimension());

		for (Carriage carriage : train.carriages) {
			IItemHandlerModifiable carriageInventory = carriage.storage.getAllItems();
			if (carriageInventory == null)
				continue;

			// Import from station
			for (Entry<BlockPos, GlobalPackagePort> entry : connectedPorts.entrySet()) {
				GlobalPackagePort port = entry.getValue();
				BlockPos pos = entry.getKey();
				PostboxBlockEntity box = null;

				IItemHandlerModifiable postboxInventory = port.offlineBuffer;
				if (level != null && level.isLoaded(pos)
					&& level.getBlockEntity(pos) instanceof PostboxBlockEntity ppbe) {
					postboxInventory = ppbe.inventory;
					box = ppbe;
				}

				for (int slot = 0; slot < postboxInventory.getSlots(); slot++) {
					ItemStack stack = postboxInventory.getStackInSlot(slot);
					if (!PackageItem.isPackage(stack))
						continue;
					if (PackageItem.matchAddress(stack, port.address))
						continue;

					ItemStack result = ItemHandlerHelper.insertItemStacked(carriageInventory, stack, false);
					if (box != null)
						box.computerBehaviour.prepareComputerEvent(new PackageEvent(stack, "package_sent"));
					if (!result.isEmpty())
						continue;

					postboxInventory.setStackInSlot(slot, ItemStack.EMPTY);

					if (box == null) {
						port.primed = true;
					} else {
						box.spawnParticles();
					}

					Create.RAILWAYS.markTracksDirty();
				}
			}

			// Export to station
			for (int slot = 0; slot < carriageInventory.getSlots(); slot++) {
				ItemStack stack = carriageInventory.getStackInSlot(slot);
				if (!PackageItem.isPackage(stack))
					continue;

				for (Entry<BlockPos, GlobalPackagePort> entry : connectedPorts.entrySet()) {
					GlobalPackagePort port = entry.getValue();
					BlockPos pos = entry.getKey();
					PostboxBlockEntity box = null;

					if (!PackageItem.matchAddress(stack, port.address))
						continue;

					IItemHandler postboxInventory = port.offlineBuffer;
					if (level != null && level.isLoaded(pos)
						&& level.getBlockEntity(pos) instanceof PostboxBlockEntity ppbe) {
						postboxInventory = ppbe.inventory;
						box = ppbe;
					}

					ItemStack result = ItemHandlerHelper.insertItemStacked(postboxInventory, stack, false);
					if (box != null)
						box.computerBehaviour.prepareComputerEvent(new PackageEvent(stack, "package_received"));
					if (!result.isEmpty())
						continue;

					carriageInventory.setStackInSlot(slot, ItemStack.EMPTY);

					if (box == null) {
						port.primed = true;
					} else {
						box.spawnParticles();
					}

					Create.RAILWAYS.markTracksDirty();

					break;
				}
			}

		}
	}

}
